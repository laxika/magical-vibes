package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BronzeCudgels;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KamiOfTerribleSecrets;
import com.github.laxika.magicalvibes.cards.m.MobilizerMech;
import com.github.laxika.magicalvibes.cards.n.NetworkTerminal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnchorToReality.class, AncestralKatana.class, BronzeCudgels.class, Forest.class, KamiOfTerribleSecrets.class,
        MobilizerMech.class, NetworkTerminal.class})
class AnchorToRealityTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature, finds a lower-mana-value Equipment, and scries 2")
    void findsLowerManaValueEquipmentAndScries() {
        Permanent sacrifice = addCreatureReady(player1, new KamiOfTerribleSecrets());
        AnchorToReality spell = new AnchorToReality();
        AncestralKatana found = new AncestralKatana();
        Card firstScryCard = new Forest();
        Card secondScryCard = new Forest();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(found, firstScryCard, secondScryCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.assertInGraveyard(player1, "Kami of Terrible Secrets");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(found);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == found);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactlyInAnyOrder(firstScryCard, secondScryCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepts an artifact sacrifice but does not scry for an equal-mana-value Equipment")
    void equalManaValueDoesNotScry() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new MobilizerMech());
        AnchorToReality spell = new AnchorToReality();
        AncestralKatana found = new AncestralKatana();
        NetworkTerminal ineligible = new NetworkTerminal();
        Card land = new Forest();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(found, ineligible, land));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.assertInGraveyard(player1, "Mobilizer Mech");
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(found);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == found);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(ineligible, land);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Finds a lower-mana-value Vehicle and scries after putting it onto the battlefield")
    void findsVehicleAndScries() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        MobilizerMech found = new MobilizerMech();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of(new AnchorToReality()));
        harness.setLibrary(player1, List.of(found, first, second));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.assertInGraveyard(player1, "Network Terminal");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mobilizer Mech");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == found)
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isFalse());
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactlyInAnyOrder(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can find a higher-mana-value Vehicle without scrying")
    void higherManaValueDoesNotRestrictSearchOrScry() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BronzeCudgels());
        harness.setHand(player1, List.of(new AnchorToReality()));
        harness.setLibrary(player1, List.of(new MobilizerMech(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Mobilizer Mech");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May fail to find an eligible card and does not scry")
    void mayDeclineSearchWithoutScry() {
        Permanent sacrifice = addCreatureReady(player1, new KamiOfTerribleSecrets());
        AncestralKatana eligible = new AncestralKatana();
        Forest land = new Forest();
        harness.setHand(player1, List.of(new AnchorToReality()));
        harness.setLibrary(player1, List.of(eligible, land));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(eligible, land);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Finishes without scrying when the library has no Equipment or Vehicle")
    void noEligibleCardsDoesNotScry() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        KamiOfTerribleSecrets creature = new KamiOfTerribleSecrets();
        Forest land = new Forest();
        harness.setHand(player1, List.of(new AnchorToReality()));
        harness.setLibrary(player1, List.of(creature, land));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast without sacrificing an artifact or creature")
    void sacrificeIsMandatory() {
        harness.setHand(player1, List.of(new AnchorToReality()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Anchor to Reality");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot sacrifice a land that is neither an artifact nor a creature")
    void cannotSacrificeOrdinaryLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new AnchorToReality()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Anchor to Reality");
        assertThat(gd.stack).isEmpty();
    }
}
