package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwiftfootBoots.class, GrizzlyBears.class, Unsummon.class, LlanowarElves.class})
class SwiftfootBootsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has hexproof and haste")
    void equippedCreatureHasHexproofAndHaste() {
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();

        boots.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Keywords are lost when the Boots are unattached")
    void keywordsLostWhenUnattached() {
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        boots.setAttachedTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();

        boots.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Only the equipped creature gains the keywords")
    void onlyEquippedCreatureGainsKeywords() {
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());

        Permanent equipped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        boots.setAttachedTo(equipped.getId());

        assertThat(gqs.hasKeyword(gd, other, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Equip costs one mana and grants keywords only after resolution")
    void equipResolvesAndCanTargetAlreadyEquippedCreature() {
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());

        assertThat(boots.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        harness.passBothPriorities();

        assertThat(boots.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(boots.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Moving Boots transfers hexproof and haste without curing summoning sickness")
    void movingBootsTransfersKeywordsAndAttackPermission() {
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        assertThat(als.canAttack(gd, first, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, second, player1.getId())).isFalse();

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(boots.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HEXPROOF)).isTrue();
        assertThat(als.canAttack(gd, first, player1.getId())).isFalse();
        assertThat(als.canAttack(gd, second, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Hexproof prevents an opponent's spell from targeting the equipped creature")
    void hexproofPreventsOpponentsSpell() {
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        boots.setAttachedTo(bears.getId());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(boots, bears);
    }

    @Test
    @DisplayName("Hexproof allows the controller's spell to target the equipped creature")
    void hexproofAllowsControllersSpell() {
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        boots.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(boots).doesNotContain(bears);
        assertThat(boots.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(boots.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot target a noncreature permanent")
    void equipCannotTargetNoncreature() {
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, boots.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(boots.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated during upkeep")
    void equipRequiresSorceryTiming() {
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(boots.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("A failed re-equip leaves the Boots on the original creature")
    void targetLeavingInResponseDoesNotMoveBoots() {
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        boots.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(boots.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(second);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Haste lets a summoning-sick equipped creature activate its tap ability")
    void hasteAllowsTapAbility() {
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setSummoningSick(true);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();
        harness.tapPermanent(player1, 1);

        assertThat(boots.getAttachedTo()).isEqualTo(elves.getId());
        assertThat(elves.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip cannot be activated while another ability is on the stack")
    void equipRequiresEmptyStack() {
        Permanent boots = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, bears.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(boots.getAttachedTo()).isEqualTo(bears.getId());
    }
}
