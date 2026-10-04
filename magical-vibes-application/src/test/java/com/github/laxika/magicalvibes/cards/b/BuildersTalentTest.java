package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IntoTheFloodMaw;
import com.github.laxika.magicalvibes.cards.s.ShortBow;
import com.github.laxika.magicalvibes.cards.t.ThreeTreeMascot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BuildersTalent.class, Forest.class, IntoTheFloodMaw.class, ShortBow.class, ThreeTreeMascot.class})
class BuildersTalentTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 0/4 Wall token with defender when it enters")
    void createsWallToken() {
        castBuilder();

        Permanent wall = findPermanent(player1, "Wall");
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("At level 2, a qualifying permanent entering puts a counter on a chosen creature")
    void levelTwoTriggersForNoncreatureNonlandPermanent() {
        Permanent mascot = harness.addToBattlefieldAndReturn(player1, new ThreeTreeMascot());
        Permanent builder = castBuilder();

        prepareForLeveling(player1);
        levelUp(player1, builder);

        ShortBow bow = new ShortBow();
        harness.setHand(player1, List.of(bow));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, mascot.getId());
        harness.passBothPriorities();

        assertThat(mascot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("At level 2, creature and land entries do not trigger")
    void levelTwoIgnoresCreatureAndLandEntries() {
        harness.addToBattlefield(player1, new ThreeTreeMascot());
        Permanent builder = castBuilder();

        prepareForLeveling(player1);
        levelUp(player1, builder);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.setHand(player1, List.of(new ThreeTreeMascot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When it reaches level 3, returns a noncreature nonland permanent from its graveyard")
    void levelThreeReturnsPermanentCard() {
        ShortBow bow = new ShortBow();
        harness.setGraveyard(player1, List.of(bow, new ThreeTreeMascot(), new Forest()));
        harness.setGraveyard(player2, List.of(new ShortBow()));
        Permanent builder = castBuilder();

        prepareForLeveling(player1);
        levelUp(player1, builder);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int builderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(builder);
        harness.activateAbility(player1, builderIndex, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bow.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bow.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Short Bow");
        harness.assertNotInGraveyard(player1, "Short Bow");

        Permanent wall = findPermanent(player1, "Wall");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, wall.getId());
        harness.passBothPriorities();
        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void levelOneDoesNotTriggerForQualifyingEntries() {
        castBuilder();
        harness.enterBattlefieldAndReturn(player1, new ShortBow());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void levelTwoIgnoresOpponentsQualifyingEntries() {
        Permanent builder = castBuilder();
        prepareForLeveling(player1);
        levelUp(player1, builder);
        harness.enterBattlefieldAndReturn(player2, new ShortBow());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateLevelTwoAdvancementAgain() {
        Permanent builder = castBuilder();
        prepareForLeveling(player1);
        levelUp(player1, builder);
        int builderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(builder);
        assertThatThrownBy(() -> harness.activateAbility(player1, builderIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counterTriggerResolvesAfterClassLeavesBattlefield() {
        Permanent builder = castBuilder();
        Permanent wall = findPermanent(player1, "Wall");
        prepareForLeveling(player1);
        levelUp(player1, builder);
        harness.setHand(player1, List.of(new ShortBow()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wall.getId());

        harness.setHand(player2, List.of(new IntoTheFloodMaw()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstantWithGift(player2, 0, builder.getId(), true);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Builder's Talent");
        harness.passBothPriorities();
        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotAdvanceDirectlyFromLevelOneToLevelThree() {
        Permanent builder = castBuilder();
        prepareForLeveling(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int builderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(builder);
        assertThatThrownBy(() -> harness.activateAbility(player1, builderIndex, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void levelTwoAdvancementRequiresSorceryTiming() {
        Permanent builder = castBuilder();
        prepareForLeveling(player1);
        harness.forceStep(TurnStep.UPKEEP);
        int builderIndex = gd.playerBattlefields.get(player1.getId()).indexOf(builder);
        assertThatThrownBy(() -> harness.activateAbility(player1, builderIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castBuilder() {
        harness.setHand(player1, List.of(new BuildersTalent()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Builder's Talent");
    }

    private void prepareForLeveling(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.WHITE, 3);
    }

    private void levelUp(Player player, Permanent builder) {
        int builderIndex = gd.playerBattlefields.get(player.getId()).indexOf(builder);
        harness.activateAbility(player, builderIndex, 0, null, null);
        harness.passBothPriorities();
    }
}
