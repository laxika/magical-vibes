package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BorealOutrider;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.k.KaldringTheRimestaff;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JornGodOfWinter.class, KaldringTheRimestaff.class, Forest.class,
        SnowCoveredForest.class, BorealOutrider.class})
class JornGodOfWinterTest extends BaseCardTest {

    @Test
    void attackingUntapsSnowPermanentsButNotOtherPermanents() {
        Permanent jorn = harness.addToBattlefieldAndReturn(player1, new JornGodOfWinter());
        jorn.setSummoningSick(false);
        Permanent snowForest = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        snowForest.tap();
        forest.tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(snowForest.isTapped()).isFalse();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    void targetedSnowLandPlayedFromGraveyardEntersTapped() {
        Permanent kaldring = addKaldring();
        Card snowForest = new SnowCoveredForest();
        harness.setGraveyard(player1, List.of(snowForest));

        harness.activateAbility(player1, 0, null, snowForest.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.playGraveyardLand(player1, 0);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == snowForest)
                .findFirst()
                .orElseThrow();
        assertThat(kaldring.isTapped()).isTrue();
        assertThat(entered.isTapped()).isTrue();
    }

    @Test
    void targetedSnowPermanentSpellEntersTapped() {
        addKaldring();
        Card snowCreature = new BorealOutrider();
        harness.setGraveyard(player1, List.of(snowCreature));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, null, snowCreature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() == snowCreature)
                .findFirst()
                .orElseThrow();
        assertThat(entered.isTapped()).isTrue();
    }

    @Test
    void abilityCannotTargetNonSnowPermanentCard() {
        addKaldring();
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addKaldring() {
        Permanent kaldring = harness.addToBattlefieldAndReturn(player1, new KaldringTheRimestaff());
        kaldring.setSummoningSick(false);
        return kaldring;
    }
}
