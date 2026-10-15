package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.ParadiseMantle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmedAssailant.class, ParadiseMantle.class})
class ArmedAssailantTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 and menace while equipped")
    void getsBonusWhileEquipped() {
        Permanent assailant = harness.addToBattlefieldAndReturn(player1, new ArmedAssailant());
        Permanent mantle = harness.addToBattlefieldAndReturn(player1, new ParadiseMantle());
        mantle.setAttachedTo(assailant.getId());

        assertThat(gqs.getEffectivePower(gd, assailant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, assailant)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, assailant, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Does not get the bonus while unequipped")
    void doesNotGetBonusWhileUnequipped() {
        Permanent assailant = harness.addToBattlefieldAndReturn(player1, new ArmedAssailant());

        assertThat(gqs.getEffectivePower(gd, assailant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, assailant)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, assailant, Keyword.MENACE)).isFalse();
    }

    @Test
    void multipleEquipmentDoNotMultiplyBonusAndLastEquipmentLeavingRemovesIt() {
        Permanent assailant = harness.addToBattlefieldAndReturn(player1, new ArmedAssailant());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ParadiseMantle());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ParadiseMantle());
        first.setAttachedTo(assailant.getId());
        second.setAttachedTo(assailant.getId());

        assertThat(gqs.getEffectivePower(gd, assailant)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, assailant, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.getEffectivePower(gd, assailant)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, assailant, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.getEffectivePower(gd, assailant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, assailant)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, assailant, Keyword.MENACE)).isFalse();
    }

    @Test
    void bonusFollowsEquipmentEvenWhenOpponentControlsIt() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArmedAssailant());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ArmedAssailant());
        Permanent mantle = harness.addToBattlefieldAndReturn(player2, new ParadiseMantle());
        mantle.setAttachedTo(first.getId());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isFalse();

        mantle.setAttachedTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isTrue();
    }

    @Test
    void equippedAssailantRejectsOneBlockerButAllowsTwo() {
        Permanent assailant = addCreatureReady(player1, new ArmedAssailant());
        Permanent mantle = harness.addToBattlefieldAndReturn(player1, new ParadiseMantle());
        mantle.setAttachedTo(assailant.getId());
        Permanent first = addCreatureReady(player2, new ArmedAssailant());
        Permanent second = addCreatureReady(player2, new ArmedAssailant());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void unequippedAssailantsCanBlockEachOtherAndDieToDeathtouch() {
        addCreatureReady(player1, new ArmedAssailant());
        addCreatureReady(player2, new ArmedAssailant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Armed Assailant");
        harness.assertNotOnBattlefield(player2, "Armed Assailant");
        harness.assertInGraveyard(player1, "Armed Assailant");
        harness.assertInGraveyard(player2, "Armed Assailant");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
