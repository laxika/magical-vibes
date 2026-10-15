package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecureDetention.class, GrizzlyBears.class, MindStone.class, Plains.class})
class SecureDetentionTest extends BaseCardTest {

    @Test
    @DisplayName("Secure Detention creates a 1/1 white Soldier when it enters")
    void createsSoldierWhenItEnters() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castSecureDetention(target);

        Permanent token = findPermanent(player1, "Soldier");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Secure Detention prevents the enchanted creature from attacking or blocking")
    void preventsEnchantedCreatureFromAttackingOrBlocking() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castSecureDetention(target);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Secure Detention prevents an enchanted artifact's activated abilities")
    void preventsEnchantedArtifactAbilities() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        castSecureDetention(target);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void preventsEnchantedArtifactsNonManaAbilityWithoutPayingCosts() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        castSecureDetention(target);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void canEnchantOwnPermanentAndCreatesExactlyOneSoldier() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MindStone());

        castSecureDetention(target);

        assertThat(findPermanent(player1, "Secure Detention").getAttachedTo()).isEqualTo(target.getId());
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        assertThat(countPermanents(player2, "Soldier")).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void createsNoSoldierWhenTargetIsSacrificedBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SecureDetention()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, target.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, 1, null, null);

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soldier")).isZero();
        assertThat(countPermanents(player1, "Secure Detention")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SecureDetention);
    }

    @Test
    @DisplayName("Secure Detention cannot target a nonartifact noncreature permanent")
    void rejectsInvalidTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new SecureDetention()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    private void castSecureDetention(Permanent target) {
        harness.setHand(player1, List.of(new SecureDetention()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
    }
}
