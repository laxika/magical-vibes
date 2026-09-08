package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParanoidParishBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Base 3/2 without delirium")
    void noDeliriumBaseStats() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.addToBattlefield(player1, new ParanoidParishBlade());

        Permanent blade = findBlade();
        assertThat(gqs.getEffectivePower(gd, blade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blade, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and first strike with four card types in its controller's graveyard")
    void deliriumBonusAtThreshold() {
        setDelirium();
        harness.addToBattlefield(player1, new ParanoidParishBlade());

        Permanent blade = findBlade();
        assertThat(gqs.getEffectivePower(gd, blade)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blade)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blade, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's graveyard does not count toward delirium")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.addToBattlefield(player1, new ParanoidParishBlade());

        Permanent blade = findBlade();
        assertThat(gqs.getEffectivePower(gd, blade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, blade, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Loses its delirium bonus when the graveyard drops below four card types")
    void losesDeliriumBonusWhenGraveyardChanges() {
        setDelirium();
        harness.addToBattlefield(player1, new ParanoidParishBlade());

        Permanent blade = findBlade();
        assertThat(gqs.getEffectivePower(gd, blade)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, blade, Keyword.FIRST_STRIKE)).isTrue();

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        assertThat(gqs.getEffectivePower(gd, blade)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, blade, Keyword.FIRST_STRIKE)).isFalse();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
    }

    private Permanent findBlade() {
        return findPermanent(player1, "Paranoid Parish-Blade");
    }
}
