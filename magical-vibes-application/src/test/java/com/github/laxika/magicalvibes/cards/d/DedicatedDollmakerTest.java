package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cromat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DedicatedDollmaker.class, Cromat.class, GrizzlyBears.class, Plains.class, Pacifism.class})
class DedicatedDollmakerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles another nonland nontoken permanent and gives its controller a lasting artifact copy")
    void exilesTargetAndCreatesCopyForItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Cromat());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard());

        Permanent dollmaker = harness.enterBattlefieldAndReturn(player1, new DedicatedDollmaker());

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(target.getId());
        assertThat(choice.validIds()).doesNotContain(dollmaker.getId(), token.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(plains);
        Permanent copy = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && !permanent.getId().equals(token.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(copy.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(copy.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(copy);
    }

    @Test
    @DisplayName("The indestructible ability affects only your tokens and can be activated only once")
    void grantsIndestructibleToTokensOnlyOnce() {
        Permanent dollmaker = harness.addToBattlefieldAndReturn(player1, new DedicatedDollmaker());
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int dollmakerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dollmaker);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, dollmakerIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, dollmaker, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, dollmakerIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void entersWithoutExilingAnythingWhenThereAreNoLegalTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard());

        Permanent dollmaker = harness.enterBattlefieldAndReturn(player1, new DedicatedDollmaker());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(land, token, dollmaker);
    }

    @Test
    void auraCopyEntersAttachedToTheCreatureChosenByItsController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(creature.getId());

        harness.enterBattlefieldAndReturn(player1, new DedicatedDollmaker());
        harness.handlePermanentChosen(player1, aura.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice attachmentChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(attachmentChoice).isNotNull();
        assertThat(attachmentChoice.validIds()).contains(creature.getId());
        harness.handlePermanentChosen(player2, creature.getId());

        Permanent copy = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(copy.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(copy.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aura);
    }

    @Test
    void indestructibleAffectsOnlyTokensControlledAtResolution() {
        harness.addToBattlefield(player1, new DedicatedDollmaker());
        Permanent ownToken = harness.addToBattlefieldAndReturn(player1, tokenCard());
        Permanent opposingToken = harness.addToBattlefieldAndReturn(player2, tokenCard());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        Permanent tokenCreatedInResponse = harness.addToBattlefieldAndReturn(player1, tokenCard());
        harness.passBothPriorities();
        Permanent laterToken = harness.addToBattlefieldAndReturn(player1, tokenCard());

        assertThat(gqs.hasKeyword(gd, ownToken, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, tokenCreatedInResponse, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingToken, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterToken, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Card tokenCard() {
        Card token = new Card();
        token.setName("Test Token");
        token.setType(CardType.CREATURE);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }
}
