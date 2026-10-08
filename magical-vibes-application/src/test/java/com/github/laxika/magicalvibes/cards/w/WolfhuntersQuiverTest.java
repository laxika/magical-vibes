package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HinterlandHermit;
import com.github.laxika.magicalvibes.cards.s.SanctuaryCat;
import com.github.laxika.magicalvibes.cards.s.SorinLordOfInnistrad;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WolfhuntersQuiver.class, SanctuaryCat.class, HinterlandHermit.class})
class WolfhuntersQuiverTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip ability attaches Wolfhunter's Quiver to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent quiver = addQuiverReady(player1);
        Permanent creature = addCreatureReady(player1, new SanctuaryCat());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(quiver.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature can tap to deal 1 damage to target creature")
    void grantedAbilityDeals1DamageToCreature() {
        Permanent creature = addCreatureReady(player1, new SanctuaryCat());
        addEquippedQuiver(player1, creature);

        Permanent targetCreature = addCreatureReady(player2, new SanctuaryCat());

        harness.activateAbility(player1, 0, 0, null, targetCreature.getId());
        harness.passBothPriorities();

        assertThat(targetCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equipped creature can tap to deal 1 damage to a player")
    void grantedAbilityDeals1DamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new SanctuaryCat());
        addEquippedQuiver(player1, creature);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Equipped creature can tap to deal 3 damage to target Werewolf creature")
    void grantedAbilityDeals3DamageToWerewolf() {
        Permanent creature = addCreatureReady(player1, new SanctuaryCat());
        addEquippedQuiver(player1, creature);

        Permanent werewolf = addCreatureReady(player2, new HinterlandHermit());

        harness.activateAbility(player1, 0, 1, null, werewolf.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(werewolf.getId()));
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Werewolf hunter ability cannot target non-Werewolf creatures")
    void werewolfAbilityCannotTargetNonWerewolf() {
        Permanent creature = addCreatureReady(player1, new SanctuaryCat());
        addEquippedQuiver(player1, creature);

        Permanent targetCreature = addCreatureReady(player2, new SanctuaryCat());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Werewolf creature");
    }

    @Test
    @DisplayName("Summoning sick creature cannot use granted tap abilities")
    void summoningSickCreatureCannotUseGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SanctuaryCat());
        addEquippedQuiver(player1, creature);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Already tapped creature cannot use granted tap abilities")
    void tappedCreatureCannotUseGrantedAbility() {
        Permanent creature = addCreatureReady(player1, new SanctuaryCat());
        creature.tap();
        addEquippedQuiver(player1, creature);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Creature loses granted abilities when Wolfhunter's Quiver is removed")
    void creatureLosesAbilityWhenQuiverRemoved() {
        Permanent creature = addCreatureReady(player1, new SanctuaryCat());
        Permanent quiver = addEquippedQuiver(player1, creature);

        gd.playerBattlefields.get(player1.getId()).remove(quiver);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Damage from any-target ability is dealt by the equipped creature")
    void damageSourceIsEquippedCreature() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new SanctuaryCat());
        addEquippedQuiver(player1, creature);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains("damage from Sanctuary Cat")).isTrue();
        assertThat(gameLogContains("damage from Wolfhunter's Quiver")).isFalse();
    }

    @Test
    @DisplayName("An activated damage ability resolves after the Equipment leaves")
    void activatedAbilitySurvivesEquipmentRemoval() {
        Permanent creature = addCreatureReady(player1, new SanctuaryCat());
        Permanent quiver = addEquippedQuiver(player1, creature);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(quiver);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Werewolf hunter ability cannot target a player")
    void werewolfAbilityCannotTargetPlayer() {
        Permanent creature = addCreatureReady(player1, new SanctuaryCat());
        addEquippedQuiver(player1, creature);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Werewolf hunter ability can target a Werewolf you control")
    void werewolfAbilityCanTargetOwnWerewolf() {
        Permanent creature = addCreatureReady(player1, new SanctuaryCat());
        addEquippedQuiver(player1, creature);
        Permanent werewolf = addCreatureReady(player1, new HinterlandHermit());

        harness.activateAbility(player1, 0, 1, null, werewolf.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(werewolf);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof HinterlandHermit);
    }

    @Test
    @DisplayName("Reequipping transfers both granted abilities to the new creature")
    void reequippingTransfersGrantedAbilities() {
        Permanent first = addCreatureReady(player1, new SanctuaryCat());
        Permanent quiver = addEquippedQuiver(player1, first);
        Permanent second = addCreatureReady(player1, new SanctuaryCat());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 1, null, second.getId());
        harness.passBothPriorities();

        assertThat(quiver.getAttachedTo()).isEqualTo(second.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 2, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(second.isTapped()).isTrue();
        second.untap();
        Permanent werewolf = addCreatureReady(player2, new HinterlandHermit());
        harness.activateAbility(player1, 2, 1, null, werewolf.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(werewolf);
    }

    @Test
    @CardUsed({SorinLordOfInnistrad.class})
    @DisplayName("Any-target ability deals 1 damage to a planeswalker")
    void grantedAbilityDeals1DamageToPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new SanctuaryCat());
        addEquippedQuiver(player1, creature);
        Permanent sorin = harness.addToBattlefieldAndReturn(player2, new SorinLordOfInnistrad());
        sorin.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 0, null, sorin.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Werewolf hunter ability deals exactly 3 damage")
    void werewolfAbilityDealsExactlyThreeDamage() {
        Permanent creature = addCreatureReady(player1, new SanctuaryCat());
        addEquippedQuiver(player1, creature);
        Permanent werewolf = addCreatureReady(player2, new HinterlandHermit());
        werewolf.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, 1, null, werewolf.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(werewolf);
        assertThat(werewolf.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Werewolf remains a legal target after transforming to its Werewolf back face")
    void werewolfTargetRemainsLegalAfterTransforming() {
        Permanent creature = addCreatureReady(player1, new SanctuaryCat());
        addEquippedQuiver(player1, creature);
        Permanent werewolf = addCreatureReady(player2, new HinterlandHermit());

        harness.activateAbility(player1, 0, 1, null, werewolf.getId());
        werewolf.setCard(werewolf.getCard().getBackFaceCard());
        werewolf.setTransformed(true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(werewolf);
    }

    private Permanent addQuiverReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new WolfhuntersQuiver());
    }

    private Permanent addEquippedQuiver(Player player, Permanent creature) {
        Permanent quiver = addQuiverReady(player);
        quiver.setAttachedTo(creature.getId());
        return quiver;
    }
}
