package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BroodmateTyrant.class})
class BroodmateTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 5/5 red Dragon token with flying")
    void etbCreatesDragonToken() {
        harness.setHand(player1, List.of(new BroodmateTyrant()));
        addManaForCast();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = dragonTokens(player1);
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getEffectivePower()).isEqualTo(5);
        assertThat(token.getEffectiveToughness()).isEqualTo(5);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Encore creates an untapped hasty copy before attackers are declared")
    void encoreCreatesUntappedHastyTokenCopy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new BroodmateTyrant()));
        addManaForEncore();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Broodmate Tyrant");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Broodmate Tyrant"));
    }

    @Test
    @DisplayName("Encore sacrifices its token copy at the next end step")
    void encoreSacrificesTokenCopyAtNextEndStep() {
        harness.setGraveyard(player1, List.of(new BroodmateTyrant()));
        addManaForEncore();

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Broodmate Tyrant")).hasSize(1);

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Broodmate Tyrant")).isEmpty();
        assertThat(dragonTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Encore copies trigger the Dragon ability without granting haste to the Dragon")
    void encoreCopyCreatesOrdinaryDragon() {
        harness.setGraveyard(player1, List.of(new BroodmateTyrant()));
        addManaForEncore();
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Broodmate Tyrant")).hasSize(1);
        assertThat(dragonTokens(player1)).hasSize(1);
        Permanent dragon = dragonTokens(player1).getFirst();
        assertThat(dragon.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(dragon.isTapped()).isFalse();
        assertThat(dragon.isAttacking()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An able encore copy must be declared as an attacker")
    void encoreCopyMustAttackIfAble() {
        harness.setGraveyard(player1, List.of(new BroodmateTyrant()));
        addManaForEncore();
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        Permanent copy = findPermanent(player1, "Broodmate Tyrant");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(copy);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService()
                .getMustAttackIndices(gd, player1.getId(), List.of(index))).contains(index);
    }

    @Test
    @DisplayName("Encore sacrifice uses a delayed trigger that can be responded to")
    void encoreSacrificeWaitsForTriggerResolution() {
        harness.setGraveyard(player1, List.of(new BroodmateTyrant()));
        addManaForEncore();
        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Broodmate Tyrant")).hasSize(1);
        assertThat(gd.stack).isNotEmpty();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Broodmate Tyrant")).isEmpty();
        assertThat(dragonTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Encore cannot be activated during combat")
    void encoreRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new BroodmateTyrant()));
        addManaForEncore();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Broodmate Tyrant");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void addManaForCast() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void addManaForEncore() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private List<Permanent> dragonTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Dragon"))
                .toList();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
