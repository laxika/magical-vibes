package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScepterOfDominance.class, GrizzlyBears.class, Forest.class, GloriousAnthem.class})
class ScepterOfDominanceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability taps target creature")
    void resolvingTapsTargetCreature() {
        addReadyScepter(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps Scepter of Dominance and consumes mana")
    void activatingTapsScepterAndConsumesMana() {
        Permanent scepter = addReadyScepter(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(scepter.isTapped()).isTrue();
        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can tap a land")
    void canTapLand() {
        addReadyScepter(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap an enchantment (any permanent is a legal target)")
    void canTapEnchantment() {
        addReadyScepter(player1);
        Permanent enchantment = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, enchantment.getId());
        harness.passBothPriorities();

        assertThat(enchantment.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyScepter(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A newly entered noncreature Scepter can activate immediately")
    void newlyEnteredScepterCanActivate() {
        harness.addToBattlefield(player1, new ScepterOfDominance());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Scepter can target itself even though paying the cost taps it")
    void canTargetItself() {
        Permanent scepter = addReadyScepter(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, scepter.getId());
        harness.passBothPriorities();

        assertThat(scepter.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped permanent remains a legal target")
    void canTargetTappedPermanent() {
        addReadyScepter(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate a tapped Scepter")
    void cannotActivateTappedScepter() {
        Permanent scepter = addReadyScepter(player1);
        scepter.tap();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability resolves after Scepter leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent scepter = addReadyScepter(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(scepter);
        gd.playerGraveyards.get(player1.getId()).add(scepter.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyScepter(Player player) {
        return addCreatureReady(player, new ScepterOfDominance());
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private Permanent addReadyEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GloriousAnthem());
    }
}
