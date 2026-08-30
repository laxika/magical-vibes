package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DraugrsHelmTest extends BaseCardTest {

    @Test
    @DisplayName("Paying the ETB cost creates and equips a Zombie Berserker")
    void payingEtbCostCreatesAndEquipsZombieBerserker() {
        Permanent helm = castHelmWithMana(3, 2);

        harness.handleMayAbilityChosen(player1, true);

        Permanent zombieBerserker = findPermanents(player1, "Zombie").getFirst();
        assertThat(helm.getAttachedTo()).isEqualTo(zombieBerserker.getId());
        assertThat(gqs.getEffectivePower(gd, zombieBerserker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, zombieBerserker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, zombieBerserker, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Declining the ETB cost creates no Zombie Berserker")
    void decliningEtbCostCreatesNoZombieBerserker() {
        Permanent helm = castHelmWithMana(3, 2);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        assertThat(helm.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip {4} gives the equipped creature +2/+2 and menace")
    void equipAttachesHelmAndGrantsBonus() {
        Permanent helm = addHelmReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    private Permanent castHelmWithMana(int colorless, int black) {
        harness.setHand(player1, List.of(new DraugrsHelm()));
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.BLACK, black);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof DraugrsHelm)
                .findFirst()
                .orElseThrow();
    }

    private Permanent addHelmReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new DraugrsHelm());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
