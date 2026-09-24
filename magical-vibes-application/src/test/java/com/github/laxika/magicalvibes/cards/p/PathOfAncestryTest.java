package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BalefulStrix;
import com.github.laxika.magicalvibes.cards.c.CordialVampire;
import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;




@CardUsed({PathOfAncestry.class, GrizzlyBears.class, ElvishMystic.class, BalefulStrix.class, LlanowarElves.class, EdgarMarkov.class, CordialVampire.class})
class PathOfAncestryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.addToBattlefield(player1, new PathOfAncestry());

        assertThat(findPermanent(player1, "Path of Ancestry").isTapped()).isTrue();
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
        harness.handleListChoice(player1, ManaColor.GREEN.name());
    }

    private void prepareCommanderAndPath() {
        GrizzlyBears commander = new GrizzlyBears();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));

        harness.addToBattlefield(player1, new PathOfAncestry());
    }
    @Test
    void scriesWhenCommanderTypeCreatureUsesPathMana() {
        commander();
        harness.addToBattlefield(player1, new PathOfAncestry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
    }

    @Test
    void doesNotTriggerForCreatureCastWithManaFromAnotherSource() {
        commander();
        harness.addToBattlefield(player1, new PathOfAncestry());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotTriggerForCreatureThatDoesNotShareCommanderType() {
        commander();
        harness.addToBattlefield(player1, new PathOfAncestry());
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new LlanowarElves()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Card commander() {
        Card card = new Card();
        card.setName("Test Bear Commander");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.BEAR));
        card.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        card.setManaCost("{1}");
        card.setColorIdentity(List.of(CardColor.GREEN));
        card.setPower(2);
        card.setToughness(2);
        card.setOwnerId(player1.getId());
        card.freeze();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), card);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(card)));
        return card;
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
    @DisplayName("Scries when its mana casts a creature sharing a type with the commander")
    void scriesOnMatchingCreatureSpell() {
        gd.playerCommanders.put(player1.getId(), List.of(new EdgarMarkov()));
        Permanent path = harness.enterBattlefieldAndReturn(player1, new PathOfAncestry());
        path.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        Card creature = new CordialVampire();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new com.github.laxika.magicalvibes.service.interaction.InteractionAnswer.ScryOrder(
                        List.of(0), List.of()));
    }

    @Test
    @DisplayName("Does not scry for a creature with no shared type")
    void doesNotScryOnNonmatchingCreatureSpell() {
        gd.playerCommanders.put(player1.getId(), List.of(new EdgarMarkov()));
        Permanent path = harness.enterBattlefieldAndReturn(player1, new PathOfAncestry());
        path.untap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

}
