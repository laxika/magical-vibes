package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KayaIntangibleSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("+2 makes each opponent lose 3 life and gains 3 life")
    void plusTwoDrainsFixedAmounts() {
        Permanent kaya = addReadyKaya(4);
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLife + 3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 3);
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("0 draws two cards and offers scry only to opponents")
    void zeroDrawsAndOffersOpponentScry() {
        Permanent kaya = addReadyKaya(4);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player2.getId());
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("-3 exiles a non-Aura enchantment and creates a white 1/1 Spirit copy")
    void minusThreeCopiesNonAuraEnchantmentAsSpirit() {
        Permanent kaya = addReadyKaya(4);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence());

        harness.activateAbility(player1, 0, 2, null, enchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(enchantment.getCard());
        Permanent token = findPermanents(player1, "Aura of Silence").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-3 exiles an Aura without creating a copy")
    void minusThreeDoesNotCopyAura() {
        addReadyKaya(4);
        Card auraCard = new Pacifism();
        auraCard.setOwnerId(player2.getId());
        Permanent aura = new Permanent(auraCard);
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.activateAbility(player1, 0, 2, null, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aura);
        assertThat(findPermanents(player1, "Pacifism")).isEmpty();
    }

    @Test
    @DisplayName("-3 cannot target a noncreature artifact")
    void minusThreeRejectsNoncreatureArtifact() {
        addReadyKaya(4);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyKaya(int loyalty) {
        Permanent kaya = new Permanent(new KayaIntangibleSlayer());
        kaya.setCounterCount(CounterType.LOYALTY, loyalty);
        kaya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kaya);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return kaya;
    }
}
