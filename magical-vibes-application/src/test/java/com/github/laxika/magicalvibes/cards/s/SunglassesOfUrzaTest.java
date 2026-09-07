package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunglassesOfUrza.class, DragonWhelp.class, GrizzlyBears.class, LightningBolt.class})
class SunglassesOfUrzaTest extends BaseCardTest {

    @Test
    @DisplayName("A red spell can be cast paying its {R} with white mana")
    void castsRedSpellPayingRedWithWhite() {
        harness.addToBattlefield(player1, new SunglassesOfUrza());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
    }

    @Test
    @DisplayName("Only the white mana actually needed for the red pip is spent as red")
    void spendsOnlyTheWhiteNeededForTheRedPip() {
        harness.addToBattlefield(player1, new SunglassesOfUrza());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, bearsId);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("A red spell is playable with only white mana while Sunglasses is in play")
    void redSpellPlayableWithOnlyWhiteWhenPresent() {
        harness.addToBattlefield(player1, new SunglassesOfUrza());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Card lightningBolt = gd.playerHands.get(player1.getId()).getFirst();
        ManaPool pool = gd.playerManaPools.get(player1.getId());

        assertThat(harness.getGameActionAvailabilityService()
                .isCardPlayable(gd, player1.getId(), lightningBolt, pool, 0)).isTrue();
    }

    @Test
    @DisplayName("Without Sunglasses, white mana cannot make a red spell playable")
    void redSpellNotPlayableWithOnlyWhiteWithoutSunglasses() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Card lightningBolt = gd.playerHands.get(player1.getId()).getFirst();
        ManaPool pool = gd.playerManaPools.get(player1.getId());

        assertThat(harness.getGameActionAvailabilityService()
                .isCardPlayable(gd, player1.getId(), lightningBolt, pool, 0)).isFalse();
    }

    @Test
    @DisplayName("A {R} activated ability can be paid with white mana")
    void activatesRedAbilityPayingWithWhite() {
        Permanent dragonWhelp = addCreatureReady(player1, new DragonWhelp());
        harness.addToBattlefield(player1, new SunglassesOfUrza());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragonWhelp.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Without Sunglasses, white mana cannot pay a {R} ability")
    void cannotPayRedAbilityWithWhiteWithoutSunglasses() {
        addCreatureReady(player1, new DragonWhelp());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Sunglasses only grants the permission to its controller")
    void doesNotGrantPermissionToOpponent() {
        harness.addToBattlefield(player1, new SunglassesOfUrza());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        Card lightningBolt = gd.playerHands.get(player2.getId()).getFirst();
        ManaPool pool = gd.playerManaPools.get(player2.getId());

        assertThat(harness.getGameActionAvailabilityService()
                .isCardPlayable(gd, player2.getId(), lightningBolt, pool, 0)).isFalse();
    }
}
