package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({HandsOfBinding.class, ArmoredTransport.class})
class HandsOfBindingTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the target creature and skips its next untap when cipher is declined")
    void tapsAndSkipsNextUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        harness.setHand(player1, List.of(new HandsOfBinding()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Hands of Binding");
    }

    @Test
    @DisplayName("Encodes on a creature and casts a copy after combat damage")
    void encodesAndCastsCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        Permanent attacker = addCreatureReady(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new HandsOfBinding()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Hands of Binding"));
        harness.assertNotInGraveyard(player1, "Hands of Binding");

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.exiledCards).hasSize(1);
    }

    @Test
    void alreadyTappedCreatureSkipsOnlyItsNextUntapStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        target.tap();
        harness.setHand(player1, List.of(new HandsOfBinding()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, false);

        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotTargetCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new HandsOfBinding()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
        harness.assertInHand(player1, "Hands of Binding");
    }

    @Test
    void illegalTargetPreventsEncoding() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        addCreatureReady(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new HandsOfBinding()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Hands of Binding");
    }

    @Test
    void acceptingCipherWithoutACreatureLeavesSpellInGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        harness.setHand(player1, List.of(new HandsOfBinding()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Hands of Binding");
    }

    @Test
    void cipherCopyResolvesWithoutManaAndCannotEncodeItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        Permanent attacker = addCreatureReady(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new HandsOfBinding()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        target.untap();
        target.setSkipUntapCount(0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).hasSize(1);
        harness.assertNotInGraveyard(player1, "Hands of Binding");
    }

    @Test
    void combatDamageCopyCanBeDeclined() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        Permanent attacker = addCreatureReady(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new HandsOfBinding()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());
        target.setSkipUntapCount(0);

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getSkipUntapCount()).isZero();
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
