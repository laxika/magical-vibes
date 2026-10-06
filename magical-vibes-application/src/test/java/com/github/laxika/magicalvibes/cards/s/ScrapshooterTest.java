package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.InnkeepersTalent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scrapshooter.class, ShortBow.class, InnkeepersTalent.class})
class ScrapshooterTest extends BaseCardTest {

    @Test
    void withoutGiftDoesNotDrawOrDestroy() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ShortBow());
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        cast(false, null);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    void promisedGiftDrawsAndDestroysOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ShortBow());
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        cast(true, artifact.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
    }

    @Test
    void promisedGiftCannotTargetYourOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ShortBow());
        harness.setHand(player1, List.of(new Scrapshooter()));
        addMana();

        assertThatThrownBy(() -> harness.castCreatureWithGift(player1, 0, artifact.getId(), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    void promisedGiftDestroysOpponentsEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new InnkeepersTalent());
        harness.setLibrary(player2, List.of(new Scrapshooter()));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        cast(true, enchantment.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantment);
        harness.assertInGraveyard(player2, "Innkeeper's Talent");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
    }

    @Test
    void promisedGiftStillDrawsWithNoArtifactOrEnchantmentToTarget() {
        harness.setLibrary(player2, List.of(new Scrapshooter()));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        cast(true, null);

        harness.assertOnBattlefield(player1, "Scrapshooter");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
    }

    @Test
    void giftDrawSurvivesDestructionTargetLeavingBattlefield() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ShortBow());
        harness.setLibrary(player2, List.of(new Scrapshooter()));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        harness.setHand(player1, List.of(new Scrapshooter()));
        addMana();
        harness.castCreatureWithGift(player1, 0, artifact.getId(), true);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Scrapshooter");

        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerGraveyards.get(player2.getId()).add(artifact.getCard());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
    }

    @Test
    void decliningGiftDoesNotCreateATargetChoiceOrEnterTrigger() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ShortBow());
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        harness.setHand(player1, List.of(new Scrapshooter()));
        addMana();

        harness.castCreatureWithGift(player1, 0, null, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scrapshooter");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    private void cast(boolean giftPromised, UUID targetId) {
        harness.setHand(player1, List.of(new Scrapshooter()));
        addMana();
        harness.castCreatureWithGift(player1, 0, targetId, giftPromised);
        resolveAllTriggers();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
