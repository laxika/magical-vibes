package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FetchQuest;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrambleFamiliar.class, FetchQuest.class, Forest.class, GrizzlyBears.class, Pacifism.class, Shock.class})
class BrambleFamiliarTest extends BaseCardTest {

    @Test
    void tapsForGreen() {
        Permanent familiar = addCreatureReady(player1, new BrambleFamiliar());
        forceMainPhase();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(familiar.isTapped()).isTrue();
    }

    @Test
    void discardingAControllerCardReturnsItToItsOwnersHand() {
        BrambleFamiliar card = new BrambleFamiliar();
        Permanent familiar = addCreatureReady(player1, card);
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        forceMainPhase();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(familiar);
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    void adventureMillsSevenAndPutsAChosenCreatureEnchantmentOrLandOntoTheBattlefield() {
        BrambleFamiliar card = new BrambleFamiliar();
        Card creature = new GrizzlyBears();
        Card enchantment = new Pacifism();
        Card land = new Forest();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(creature, new Shock(), enchantment, land,
                new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(creature));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantment, land);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCreatesNoChoiceWhenNoEligibleCardWasMilled() {
        BrambleFamiliar card = new BrambleFamiliar();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
