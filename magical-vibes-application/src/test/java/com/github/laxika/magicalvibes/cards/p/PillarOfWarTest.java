package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.n.NyxbornShieldmate;
import com.github.laxika.magicalvibes.cards.e.EpharasRadiance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PillarOfWar.class, EpharasRadiance.class, NyxbornShieldmate.class})
class PillarOfWarTest extends BaseCardTest {

    private Permanent addPillarReady() {
        return addCreatureReady(player1, new PillarOfWar());
    }

    private Permanent attachRadiance(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EpharasRadiance());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    @DisplayName("Cannot attack while unenchanted")
    void cannotAttackWhileUnenchanted() {
        addPillarReady();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can attack while enchanted")
    void canAttackWhileEnchanted() {
        Permanent pillar = addPillarReady();
        attachRadiance(pillar);
        harness.addToBattlefield(player2, new NyxbornShieldmate());

        declareAttackers(List.of(0));

        assertThat(pillar.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cannot attack after the Aura leaves")
    void cannotAttackAfterAuraLeaves() {
        Permanent pillar = addPillarReady();
        Permanent aura = attachRadiance(pillar);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An opponent-controlled Aura enables attacking")
    void canAttackWithOpponentsAura() {
        Permanent pillar = addPillarReady();
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new EpharasRadiance());
        aura.setAttachedTo(pillar.getId());
        harness.addToBattlefield(player2, new NyxbornShieldmate());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(pillar.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("An Aura on another creature does not enable attacking")
    void cannotAttackWhenOnlyAnotherCreatureIsEnchanted() {
        addPillarReady();
        Permanent other = addCreatureReady(player1, new NyxbornShieldmate());
        attachRadiance(other);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An enchantment creature without bestow does not enchant Pillar")
    void cannotAttackWithUnattachedEnchantmentCreature() {
        addPillarReady();
        harness.addToBattlefield(player1, new NyxbornShieldmate());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A bestowed creature counts as an Aura for the attack permission")
    void canAttackWithBestowedAura() {
        Permanent pillar = addPillarReady();
        harness.setHand(player1, List.of(new NyxbornShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castWithAlternateCost(player1, 0, pillar.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new NyxbornShieldmate());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(pillar.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Removing one Aura leaves the attack permission while another remains")
    void canAttackWhileAnotherAuraRemains() {
        Permanent pillar = addPillarReady();
        Permanent first = attachRadiance(pillar);
        attachRadiance(pillar);
        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.addToBattlefield(player2, new NyxbornShieldmate());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(pillar.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Being enchanted does not bypass summoning sickness")
    void cannotAttackWhileSummoningSickEvenWhenEnchanted() {
        Permanent pillar = harness.addToBattlefieldAndReturn(player1, new PillarOfWar());
        pillar.setSummoningSick(true);
        attachRadiance(pillar);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Being enchanted does not allow a tapped Pillar to attack")
    void cannotAttackWhileTappedEvenWhenEnchanted() {
        Permanent pillar = addPillarReady();
        attachRadiance(pillar);
        pillar.setTapped(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
