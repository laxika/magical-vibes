package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmeriasCall.class, EmeriaShatteredSkyclave.class, GrizzlyBears.class, SerraAngel.class})
class EmeriasCallTest extends BaseCardTest {

    @Test
    @DisplayName("Emeria's Call creates two flying Angel Warrior tokens")
    void createsAngelWarriorTokens() {
        castEmeriasCall();

        List<Permanent> tokens = findPermanents(player1, "Angel Warrior");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes())
                    .containsExactly(CardSubtype.ANGEL, CardSubtype.WARRIOR);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Emeria's Call grants non-Angel creatures indestructible until your next turn")
    void grantsIndestructibleToNonAngelsUntilNextTurn() {
        Permanent nonAngel = addCreatureReady(player1, new GrizzlyBears());
        Permanent angel = addCreatureReady(player1, new SerraAngel());

        castEmeriasCall();

        assertThat(gqs.hasKeyword(gd, nonAngel, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, nonAngel, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Emeria, Shattered Skyclave can enter untapped by paying 3 life and produces white mana")
    void backFaceEntersUntappedForThreeLifeAndProducesWhiteMana() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new EmeriasCall()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        gs.playCard(gd, player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(land.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.WHITE)).isEqualTo(1);
    }

    private void castEmeriasCall() {
        harness.setHand(player1, List.of(new EmeriasCall()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
