package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DancingSword.class, DoomBlade.class, GrizzlyBears.class})
class DancingSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent sword = attachSword(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, sword)).isFalse();
    }

    @Test
    @DisplayName("Declining the death trigger leaves the Equipment on the battlefield")
    void decliningTransformationLeavesEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent sword = attachSword(player1, creature);

        destroyCreature(creature);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sword);
        assertThat(gqs.isCreature(gd, sword)).isFalse();
        assertThat(gqs.permanentHasSubtype(sword, CardSubtype.EQUIPMENT)).isTrue();
        assertThat(sword.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(sword.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Accepting the death trigger makes it a 2/1 Construct artifact with flying and ward")
    void acceptingTransformationMakesArtifactCreatureWithWard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent sword = attachSword(player1, creature);

        destroyCreature(creature);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sword)).isTrue();
        assertThat(gqs.isArtifact(gd, sword)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sword)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sword)).isEqualTo(1);
        assertThat(GameQueryService.permanentHasSubtype(sword, CardSubtype.CONSTRUCT)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(sword, CardSubtype.EQUIPMENT)).isFalse();
        assertThat(gqs.hasKeyword(gd, sword, Keyword.FLYING)).isTrue();
        assertThat(sword.getAttachedTo()).isNull();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, sword.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sword);
    }

    private Permanent attachSword(Player player, Permanent creature) {
        Permanent sword = harness.addToBattlefieldAndReturn(player, new DancingSword());
        sword.setAttachedTo(creature.getId());
        return sword;
    }

    private void destroyCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
    }
}
