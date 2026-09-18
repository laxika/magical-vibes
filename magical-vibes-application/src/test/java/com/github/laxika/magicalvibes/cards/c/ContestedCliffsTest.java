package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.r.RavenousBaloth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ContestedCliffs.class, RavenousBaloth.class, ElvishWarrior.class})
class ContestedCliffsTest extends BaseCardTest {

    @Test
    void tappingAddsColorlessMana() {
        Permanent cliffs = addReadyCliffs();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(cliffs.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void beastFightsOpponentsCreature() {
        Permanent cliffs = addReadyCliffs();
        Permanent baloth = addCreatureReady(player1, new RavenousBaloth());
        addCreatureReady(player2, new ElvishWarrior());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID balothId = baloth.getId();
        UUID warriorId = harness.getPermanentId(player2, "Elvish Warrior");
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(balothId, warriorId));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();

        assertThat(cliffs.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Elvish Warrior");
        harness.assertOnBattlefield(player1, "Ravenous Baloth");
        assertThat(baloth.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void cannotTargetNonBeastAsFirstTarget() {
        addReadyCliffs();
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player2, new ElvishWarrior());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID opponentWarriorId = harness.getPermanentId(player2, "Elvish Warrior");
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(warrior.getId(), opponentWarriorId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Beast creature you control");
    }

    @Test
    void cannotTargetOpponentsBeastAsFirstTarget() {
        addReadyCliffs();
        Permanent opponentBeast = addCreatureReady(player2, new RavenousBaloth());
        Permanent opponentWarrior = addCreatureReady(player2, new ElvishWarrior());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(opponentBeast.getId(), opponentWarrior.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Beast creature you control");
    }

    @Test
    void cannotTargetNonCreatureAsFirstTarget() {
        Permanent cliffs = addReadyCliffs();
        Permanent opponentWarrior = addCreatureReady(player2, new ElvishWarrior());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(cliffs.getId(), opponentWarrior.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Beast creature you control");
    }

    @Test
    void cannotTargetOwnCreatureAsSecondTarget() {
        addReadyCliffs();
        Permanent baloth = addCreatureReady(player1, new RavenousBaloth());
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(baloth.getId(), warrior.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    void cannotTargetNonCreatureAsSecondTarget() {
        addReadyCliffs();
        Permanent baloth = addCreatureReady(player1, new RavenousBaloth());
        Permanent opponentCliffs = harness.addToBattlefieldAndReturn(player2, new ContestedCliffs());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(baloth.getId(), opponentCliffs.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    private Permanent addReadyCliffs() {
        return harness.addToBattlefieldAndReturn(player1, new ContestedCliffs());
    }
}
