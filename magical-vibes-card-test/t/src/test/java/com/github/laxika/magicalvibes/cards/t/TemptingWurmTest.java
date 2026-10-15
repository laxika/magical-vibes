package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemptingWurm.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class,
        HowlingMine.class, Naturalize.class, ElvishWarrior.class, Pacifism.class})
class TemptingWurmTest extends BaseCardTest {

    @Test
    void eachOpponentMayPutAnyNumberOfPermanentCardsOntoTheBattlefield() {
        TemptingWurm wurm = new TemptingWurm();
        Forest ownLand = new Forest();
        Forest opponentLand = new Forest();
        GrizzlyBears opponentCreature = new GrizzlyBears();
        HowlingMine opponentArtifact = new HowlingMine();
        Naturalize nonPermanent = new Naturalize();
        harness.setHand(player1, List.of(wurm, ownLand));
        harness.setHand(player2, List.of(opponentLand, opponentCreature, opponentArtifact, nonPermanent));

        castWurm();

        PendingInteraction.EachPlayerMayPutCardFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(
                opponentLand.getId(), opponentCreature.getId(), opponentArtifact.getId());
        harness.handleMultipleCardsChosen(player2,
                List.of(opponentLand.getId(), opponentCreature.getId(), opponentArtifact.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Forest", "Grizzly Bears", "Howling Mine");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Naturalize");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enchantmentCardsAreEligible() {
        TemptingWurm wurm = new TemptingWurm();
        GloriousAnthem opponentEnchantment = new GloriousAnthem();
        harness.setHand(player1, List.of(wurm));
        harness.setHand(player2, List.of(opponentEnchantment));

        castWurm();

        PendingInteraction.EachPlayerMayPutCardFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);
        assertThat(choice.validCardIds()).containsExactly(opponentEnchantment.getId());
        harness.handleMultipleCardsChosen(player2, List.of(opponentEnchantment.getId()));

        harness.assertOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningLeavesAllCardsInHand() {
        TemptingWurm wurm = new TemptingWurm();
        Forest ownLand = new Forest();
        Forest opponentLand = new Forest();
        harness.setHand(player1, List.of(wurm, ownLand));
        harness.setHand(player2, List.of(opponentLand));

        castWurm();
        harness.handleMultipleCardsChosen(player2, List.of());

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentMayChooseOnlySomeEligibleCards() {
        Forest chosenLand = new Forest();
        Forest unchosenLand = new Forest();
        harness.setHand(player1, List.of(new TemptingWurm()));
        harness.setHand(player2, List.of(chosenLand, unchosenLand));

        castWurm();
        harness.handleMultipleCardsChosen(player2, List.of(chosenLand.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(chosenLand.getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(unchosenLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentWithOnlyIneligibleCardsNeedsNoChoice() {
        Naturalize instant = new Naturalize();
        harness.setHand(player1, List.of(new TemptingWurm()));
        harness.setHand(player2, List.of(instant));

        castWurm();

        harness.assertOnBattlefield(player1, "Tempting Wurm");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(instant);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentWithEmptyHandNeedsNoChoice() {
        harness.setHand(player1, List.of(new TemptingWurm()));
        harness.setHand(player2, List.of());

        castWurm();

        harness.assertOnBattlefield(player1, "Tempting Wurm");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void auraCanEnchantWurmButNotCreatureEnteringWithIt() {
        Pacifism aura = new Pacifism();
        ElvishWarrior creature = new ElvishWarrior();
        harness.setHand(player1, List.of(new TemptingWurm()));
        harness.setHand(player2, List.of(creature, aura));

        castWurm();
        harness.handleMultipleCardsChosen(player2, List.of(creature.getId(), aura.getId()));

        harness.assertOnBattlefield(player2, "Elvish Warrior");
        harness.assertOnBattlefield(player2, "Pacifism");
        assertThat(findPermanent(player2, "Pacifism").getAttachedTo())
                .isEqualTo(harness.getPermanentId(player1, "Tempting Wurm"));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castWurm() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
