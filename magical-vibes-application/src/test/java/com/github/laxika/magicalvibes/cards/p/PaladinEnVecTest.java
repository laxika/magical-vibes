package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.b.Bandage;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PaladinEnVec.class, BogWraith.class, Bandage.class, GiantSpider.class, GrizzlyBears.class,
        HillGiant.class, HolyStrength.class, Shock.class, Terror.class, UnholyStrength.class})
class PaladinEnVecTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Paladin en-Vec puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PaladinEnVec()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(PaladinEnVec.class);
    }

    @Test
    @DisplayName("Cannot cast Paladin en-Vec without enough mana")
    void cannotCastWithoutMana() {
        harness.setHand(player1, List.of(new PaladinEnVec()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving puts Paladin en-Vec on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new PaladinEnVec()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof PaladinEnVec);
    }

    @Test
    @DisplayName("Paladin en-Vec enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new PaladinEnVec()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof PaladinEnVec
                        && permanent.isSummoningSick());
    }

    @Test
    @DisplayName("First strike kills 2/2 blocker before it deals regular damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new PaladinEnVec());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // First strike kills Bears before it deals damage; Paladin survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof PaladinEnVec);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Black creature cannot block Paladin en-Vec")
    void blackCreatureCannotBlockPaladin() {
        Permanent attacker = addCreatureReady(player1, new PaladinEnVec());
        attacker.setAttacking(true);

        addCreatureReady(player2, new BogWraith());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Red creature cannot block Paladin en-Vec")
    void redCreatureCannotBlockPaladin() {
        Permanent attacker = addCreatureReady(player1, new PaladinEnVec());
        attacker.setAttacking(true);

        addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block Paladin en-Vec")
    void greenCreatureCanBlockPaladin() {
        Permanent attacker = addCreatureReady(player1, new PaladinEnVec());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Paladin takes no combat damage from black creature")
    void paladinTakesNoDamageFromBlack() {
        // Black 3/3 attacker, Paladin as blocker
        Permanent attacker = addCreatureReady(player1, new BogWraith());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new PaladinEnVec());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Paladin has first strike: deals 2 to Bog Wraith (3/3 survives)
        // Bog Wraith's 3 regular damage to Paladin is prevented (protection)
        // Both survive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof BogWraith);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof PaladinEnVec);
    }

    @Test
    @DisplayName("Paladin takes no combat damage from red creature")
    void paladinTakesNoDamageFromRed() {
        // Red 3/3 attacker, Paladin as blocker
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new PaladinEnVec());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Both survive: Paladin deals 2 first strike (2 < 3), Hill Giant's 3 damage is prevented
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof HillGiant);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof PaladinEnVec);
    }

    @Test
    @DisplayName("Paladin takes normal combat damage from green creature")
    void paladinTakesNormalDamageFromGreen() {
        // Giant Spider (2/4) attacker, Paladin as blocker
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new PaladinEnVec());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Paladin deals 2 first strike (2 < 4, Giant Spider survives)
        // Giant Spider deals 2 regular damage (2 >= 2, Paladin dies — no protection from green)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GiantSpider);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof PaladinEnVec);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof PaladinEnVec);
    }

    @Test
    @DisplayName("Cannot be targeted by black instant")
    void cannotBeTargetedByBlackInstant() {
        Permanent paladin = addCreatureReady(player2, new PaladinEnVec());

        // Add valid target so spell is playable
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, paladin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Cannot be targeted by red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent paladin = addCreatureReady(player2, new PaladinEnVec());

        // Add valid target so spell is playable
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, paladin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Can be targeted by white instant")
    void canBeTargetedByWhiteInstant() {
        Permanent paladin = addCreatureReady(player1, new PaladinEnVec());

        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, paladin.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(Bandage.class);
    }

    @Test
    @DisplayName("Cannot be enchanted by black aura")
    void cannotBeEnchantedByBlackAura() {
        Permanent paladin = addCreatureReady(player2, new PaladinEnVec());

        // Add valid target so aura is playable
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, paladin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be enchanted by white aura (Holy Strength)")
    void canBeEnchantedByWhiteAura() {
        Permanent paladin = addCreatureReady(player1, new PaladinEnVec());

        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, paladin.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(HolyStrength.class);
    }
}

