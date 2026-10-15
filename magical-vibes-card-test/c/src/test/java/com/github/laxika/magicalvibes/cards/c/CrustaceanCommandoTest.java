package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrustaceanCommando.class, GrizzlyBears.class, Forest.class})
class CrustaceanCommandoTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Mutagen artifact token")
    void etbCreatesMutagenToken() {
        castCrustaceanCommando();

        Permanent mutagen = findPermanent(player1, "Mutagen");
        assertThat(mutagen.getCard().isToken()).isTrue();
        assertThat(mutagen.getCard().getType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Mutagen token sacrifices to put a +1/+1 counter on a target creature")
    void mutagenAbilityCountersTargetCreature() {
        castCrustaceanCommando();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(mutagen), 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mutagen token cannot target a noncreature permanent")
    void mutagenAbilityRejectsNoncreatureTarget() {
        castCrustaceanCommando();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(mutagen), 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("Created Mutagen has the predefined token's artifact subtype")
    void createdTokenHasMutagenSubtype() {
        castCrustaceanCommando();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
        assertThat(findPermanent(player1, "Mutagen").getCard().getSubtypes())
                .extracting(subtype -> subtype.name()).contains("MUTAGEN");
    }

    @Test
    @DisplayName("Mutagen can target an opponent's creature and is sacrificed as a cost")
    void mutagenCanCounterOpponentsCreature() {
        castCrustaceanCommando();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrustaceanCommando());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(mutagen), 0, null, creature.getId());

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Mutagen cannot pay the tap cost")
    void tappedMutagenCannotActivate() {
        castCrustaceanCommando();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent creature = findPermanent(player1, "Crustacean Commando");
        mutagen.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(mutagen), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mutagen requires one mana to activate")
    void mutagenCannotActivateWithoutMana() {
        castCrustaceanCommando();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent creature = findPermanent(player1, "Crustacean Commando");

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(mutagen), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("Mutagen cannot activate outside a main phase")
    void mutagenCannotActivateDuringCombat() {
        castCrustaceanCommando();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent creature = findPermanent(player1, "Crustacean Commando");
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(mutagen), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("Mutagen cannot activate while a spell is on the stack")
    void mutagenCannotActivateWithNonemptyStack() {
        castCrustaceanCommando();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent creature = findPermanent(player1, "Crustacean Commando");
        harness.castFromHand(player1, new CrustaceanCommando(), "{1}{U}");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(mutagen), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    private void castCrustaceanCommando() {
        harness.castFromHand(player1, new CrustaceanCommando(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
