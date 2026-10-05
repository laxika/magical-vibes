package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AyulaQueenAmongBears;
import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManaReflection;
import com.github.laxika.magicalvibes.cards.s.StripMine;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;




@CardUsed({PathOfAncestry.class, GrizzlyBears.class, ElvishMystic.class, EdgarMarkov.class,
        AyulaQueenAmongBears.class, ManaReflection.class, StripMine.class, TurnToFrog.class})
class PathOfAncestryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        Permanent path = harness.enterBattlefieldAndReturn(player1, new PathOfAncestry());

        assertThat(path.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana spent on a matching creature spell causes scry 1")
    void matchingCreatureSpellTriggersScry() {
        preparePathAndCommander();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Does not trigger for a different creature type")
    void differentCreatureTypeDoesNotTrigger() {
        preparePathAndCommander();
        harness.setHand(player1, List.of(new ElvishMystic()));
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Does not trigger when the matching creature uses mana from another source")
    void manaFromAnotherSourceDoesNotTrigger() {
        prepareCommanderAndPath();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    private void preparePathAndCommander() {
        prepareCommanderAndPath();
        Permanent path = findPermanent(player1, "Path of Ancestry");
        path.untap();
        harness.activateAbility(player1, 0, 0, null, null);
    }

    private void prepareCommanderAndPath() {
        AyulaQueenAmongBears commander = new AyulaQueenAmongBears();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));

        harness.enterBattlefieldAndReturn(player1, new PathOfAncestry());
    }

    @Test
    @DisplayName("Produces mana from the commander's color identity")
    void producesCommandIdentityMana() {
        gd.playerCommanders.put(player1.getId(), List.of(new EdgarMarkov()));
        Permanent path = harness.enterBattlefieldAndReturn(player1, new PathOfAncestry());
        path.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("WHITE", "BLACK", "RED");

        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana retains its scry trigger after Path of Ancestry is destroyed")
    void spentManaTriggersAfterSourceLeavesBattlefield() {
        preparePathAndCommander();
        Permanent path = findPermanent(player1, "Path of Ancestry");
        harness.addToBattlefield(player1, new StripMine());
        harness.activateAbility(player1, 1, 1, null, path.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Path of Ancestry");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new ElvishMystic()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Each mana produced by Path creates a separate scry trigger")
    void doubledManaCreatesTwoTriggersForOneCreatureSpell() {
        prepareCommanderAndPath();
        harness.addToBattlefield(player1, new ManaReflection());
        findPermanent(player1, "Path of Ancestry").untap();
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)).hasSize(2);
    }

    @Test
    @DisplayName("Checks the commander's current creature types when the mana is spent")
    void commanderThatBecameFrogDoesNotMatchBearSpell() {
        prepareCommanderAndPath();
        AyulaQueenAmongBears commander = (AyulaQueenAmongBears)
                gd.playerCommanders.get(player1.getId()).getFirst();
        gd.playerCommandZones.get(player1.getId()).clear();
        Permanent commanderPermanent = harness.addToBattlefieldAndReturn(player1, commander);
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, commanderPermanent.getId());
        findPermanent(player1, "Path of Ancestry").untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new ElvishMystic()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Produces no mana without a commander")
    void noCommanderProducesNoMana() {
        Permanent path = harness.enterBattlefieldAndReturn(player1, new PathOfAncestry());
        path.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(path.isTapped()).isTrue();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

}
