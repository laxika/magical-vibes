package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OldGrowthTrollTest extends BaseCardTest {

    @Test
    @DisplayName("When Old-Growth Troll dies as a creature, it returns as an Aura attached to a Forest")
    void returnsAsAuraAttachedToForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new OldGrowthTroll());

        destroyTroll();

        Permanent aura = findPermanent(player1, "Old-Growth Troll");
        assertThat(aura.getAttachedTo()).isEqualTo(forest.getId());
        assertThat(aura.getCard().isAura()).isTrue();
        assertThat(gqs.isCreature(gd, aura)).isFalse();
    }

    @Test
    @DisplayName("The returned Aura grants its enchanted Forest the two printed abilities")
    void grantsForestAbilities() {
        Permanent forest = returnTrollAttachedToForest();

        harness.activateAbility(player1, battlefieldIndex(forest), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Forest ability sacrifices the land and creates a tapped Troll Warrior")
    void forestAbilityCreatesTrollWarrior() {
        Permanent forest = returnTrollAttachedToForest();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(forest), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forest);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TROLL))
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("With no Forest to enchant, Old-Growth Troll remains in its owner's graveyard")
    void staysInGraveyardWithoutForest() {
        harness.addToBattlefield(player1, new OldGrowthTroll());

        destroyTroll();

        harness.assertInGraveyard(player1, "Old-Growth Troll");
        harness.assertNotOnBattlefield(player1, "Old-Growth Troll");
    }

    private Permanent returnTrollAttachedToForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new OldGrowthTroll());
        destroyTroll();
        return forest;
    }

    private void destroyTroll() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
