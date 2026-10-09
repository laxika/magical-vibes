package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BondedConstruct;
import com.github.laxika.magicalvibes.cards.c.CultivatorColossus;
import com.github.laxika.magicalvibes.cards.f.ForebodingStatue;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DollhouseOfHorrors.class, BondedConstruct.class, GrizzlyBears.class, LlanowarElves.class,
        CultivatorColossus.class, ForebodingStatue.class, Forest.class})
class DollhouseOfHorrorsTest extends BaseCardTest {

    @Test
    void createsAConstructArtifactTokenWithDynamicStatsAndHaste() {
        harness.addToBattlefield(player1, new DollhouseOfHorrors());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = token();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.CONSTRUCT);
        assertThat(token.getCard().getPower()).isZero();
        assertThat(token.getCard().getToughness()).isZero();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    void countsOtherConstructsForTheTokenBonus() {
        harness.addToBattlefield(player1, new DollhouseOfHorrors());
        harness.addToBattlefield(player1, new BondedConstruct());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = token();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    void choosesTheCreatureWhenActivating() {
        harness.addToBattlefield(player1, new DollhouseOfHorrors());
        Card first = new GrizzlyBears();
        Card chosen = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, chosen));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardExileCostChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(chosen).doesNotContain(first);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void doesNotCopyPowerAndToughnessDefiningAbilities() {
        harness.addToBattlefield(player1, new DollhouseOfHorrors());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new CultivatorColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent token = token();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    void paysManaTapAndExileBeforeCreatingTheToken() {
        Permanent dollhouse = harness.addToBattlefieldAndReturn(player1, new DollhouseOfHorrors());
        Card creature = new ForebodingStatue();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(dollhouse.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());

        harness.passBothPriorities();
        assertThat(token()).isNotNull();
    }

    @Test
    void constructBonusChangesAndIgnoresOpponentsConstructs() {
        harness.addToBattlefield(player1, new DollhouseOfHorrors());
        harness.addToBattlefield(player2, new ForebodingStatue());
        harness.setGraveyard(player1, List.of(new ForebodingStatue()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = token();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        harness.addToBattlefield(player1, new ForebodingStatue());
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    void hasteExpiresButTokenAndConstructBonusRemain() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        harness.addToBattlefield(player1, new DollhouseOfHorrors());
        harness.setGraveyard(player1, List.of(new ForebodingStatue()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        Permanent token = token();
        assertThat(token.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(token.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    void cannotActivateOutsideYourMainPhase() {
        harness.addToBattlefield(player1, new DollhouseOfHorrors());
        harness.setGraveyard(player1, List.of(new ForebodingStatue()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotActivateDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new DollhouseOfHorrors());
        harness.setGraveyard(player1, List.of(new ForebodingStatue()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotExileANoncreatureOrAnOpponentsCreatureAsTheCost() {
        harness.addToBattlefield(player1, new DollhouseOfHorrors());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new ForebodingStatue()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void copiedManaAbilityCanBeUsedImmediatelyWithGrantedHaste() {
        harness.addToBattlefield(player1, new DollhouseOfHorrors());
        harness.setGraveyard(player1, List.of(new ForebodingStatue()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(token().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void cannotActivateWithANonemptyStack() {
        harness.addToBattlefield(player1, new DollhouseOfHorrors());
        harness.addToBattlefield(player1, new DollhouseOfHorrors());
        harness.setGraveyard(player1, List.of(new ForebodingStatue(), new ForebodingStatue()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isTapped()).isFalse();
        harness.passBothPriorities();
    }

    @Test
    void cannotActivateWithoutMana() {
        Permanent dollhouse = harness.addToBattlefieldAndReturn(player1, new DollhouseOfHorrors());
        harness.setGraveyard(player1, List.of(new ForebodingStatue()));

        assertThatThrownBy(() -> {
            harness.activateAbility(player1, 0, null, null);
            harness.handleGraveyardCardChosen(player1, 0);
        }).isInstanceOf(IllegalStateException.class);
        assertThat(dollhouse.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    private Permanent token() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
