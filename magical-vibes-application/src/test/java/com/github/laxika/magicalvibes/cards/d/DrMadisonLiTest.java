package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrMadisonLi.class, GrizzlyBears.class, Ornithopter.class, Shock.class})
class DrMadisonLiTest extends BaseCardTest {

    @Test
    void gainsEnergyWhenControllerCastsArtifactSpell() {
        harness.addToBattlefield(player1, new DrMadisonLi());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void paysEnergyToBoostTargetCreatureAndGrantKeywords() {
        Permanent doctor = addCreatureReady(player1, new DrMadisonLi());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(doctor), 0,
                null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    void paysThreeEnergyToDrawACard() {
        Permanent doctor = addCreatureReady(player1, new DrMadisonLi());
        harness.setLibrary(player1, List.of(new Shock()));
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(doctor), 1,
                null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).contains("Shock");
    }

    @Test
    void returnsTargetArtifactFromGraveyardTapped() {
        Permanent doctor = addCreatureReady(player1, new DrMadisonLi());
        Card artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));
        gd.playerEnergyCounters.put(player1.getId(), 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(doctor), 2,
                null, artifact.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(artifact.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNonArtifactCardInGraveyard() {
        Permanent doctor = addCreatureReady(player1, new DrMadisonLi());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        gd.playerEnergyCounters.put(player1.getId(), 5);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(doctor), 2,
                null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
