package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KirtarsWrathTest extends BaseCardTest {

    @Test
    @DisplayName("Without threshold, Kirtar's Wrath destroys all creatures and creates no tokens")
    void withoutThresholdDestroysCreaturesOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new KirtarsWrath()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("With threshold, Kirtar's Wrath destroys all creatures and creates two flying Spirits")
    void withThresholdCreatesSpirits() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new KirtarsWrath()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        List<Permanent> spirits = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Spirit"))
                .toList();
        assertThat(spirits).hasSize(2);
        assertThat(spirits).allMatch(spirit -> spirit.getCard().getPower() == 1
                && spirit.getCard().getToughness() == 1
                && gqs.hasKeyword(gd, spirit, Keyword.FLYING));
    }

    @Test
    @DisplayName("Kirtar's Wrath cannot be stopped by a regeneration shield")
    void destructionCannotBeRegenerated() {
        Permanent skeleton = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());
        harness.setHand(player1, List.of(new KirtarsWrath()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(skeleton);
        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }
}
