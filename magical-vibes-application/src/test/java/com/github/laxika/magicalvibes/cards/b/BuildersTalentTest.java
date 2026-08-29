package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({BuildersTalent.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class})
class BuildersTalentTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 0/4 Wall token with defender when it enters")
    void createsWallToken() {
        castBuilder();

        Permanent wall = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Wall"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("At level 2, a qualifying permanent entering puts a counter on a chosen creature")
    void levelTwoTriggersForNoncreatureNonlandPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent builder = castBuilder();

        prepareForLeveling(player1);
        levelUp(player1, builder);

        GloriousAnthem anthem = new GloriousAnthem();
        harness.setHand(player1, List.of(anthem));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("At level 2, creature and land entries do not trigger")
    void levelTwoIgnoresCreatureAndLandEntries() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent builder = castBuilder();

        prepareForLeveling(player1);
        levelUp(player1, builder);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When it reaches level 3, returns a noncreature nonland permanent from its graveyard")
    void levelThreeReturnsPermanentCard() {
        GloriousAnthem anthem = new GloriousAnthem();
        harness.setGraveyard(player1, List.of(anthem));
        Permanent builder = castBuilder();

        prepareForLeveling(player1);
        levelUp(player1, builder);
        levelUp(player1, builder);
        levelUp(player1, builder);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(anthem.getId());

        harness.handleMultipleCardsChosen(player1, List.of(anthem.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertNotInGraveyard(player1, "Glorious Anthem");
    }

    private Permanent castBuilder() {
        harness.setHand(player1, List.of(new BuildersTalent()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BuildersTalent)
                .findFirst()
                .orElseThrow();
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
