package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CeremonialGroundbreaker.class, CouriersBriefcase.class, GrizzlyBears.class})
class CeremonialGroundbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+1 and trample")
    void equippedCreatureGetsBoostAndTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent groundbreaker = addGroundbreakerReady(player1);
        groundbreaker.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equip ability attaches Ceremonial Groundbreaker to a Citizen")
    void equipAttachesToCitizen() {
        castBriefcase();
        Permanent citizen = findPermanent(player1, "Citizen");
        Permanent groundbreaker = addGroundbreakerReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, null, citizen.getId());
        harness.passBothPriorities();

        assertThat(groundbreaker.getAttachedTo()).isEqualTo(citizen.getId());
    }

    @Test
    @DisplayName("Equip ability cannot target a non-Citizen creature")
    void equipCannotTargetNonCitizen() {
        Permanent groundbreaker = addGroundbreakerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(groundbreaker.getAttachedTo()).isNull();
    }

    private void castBriefcase() {
        harness.setHand(player1, List.of(new CouriersBriefcase()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addGroundbreakerReady(Player player) {
        Permanent groundbreaker = new Permanent(new CeremonialGroundbreaker());
        groundbreaker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(groundbreaker);
        return groundbreaker;
    }
}
