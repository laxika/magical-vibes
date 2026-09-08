package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElvenBowTest extends BaseCardTest {

    @Test
    @DisplayName("Paying the ETB cost creates and equips an Elf Warrior")
    void payingEtbCostCreatesAndEquipsElfWarrior() {
        Permanent bow = castBowWithMana(3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent elfWarrior = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(bow.getAttachedTo()).isEqualTo(elfWarrior.getId());
        assertThat(gqs.getEffectivePower(gd, elfWarrior)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elfWarrior)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elfWarrior, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Declining the ETB cost creates no Elf Warrior")
    void decliningEtbCostCreatesNoElfWarrior() {
        Permanent bow = castBowWithMana(3);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
        assertThat(bow.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip {3} attaches Elven Bow to a creature you control")
    void equipAttachesBow() {
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new ElvenBow());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bow.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    private Permanent castBowWithMana(int colorless) {
        harness.setHand(player1, List.of(new ElvenBow()));
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ElvenBow)
                .findFirst()
                .orElseThrow();
    }
}
