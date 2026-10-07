package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PearlOfWisdom;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({StormchasersTalent.class, Shock.class, PearlOfWisdom.class})
class StormchasersTalentTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 blue and red Otter token with prowess when it enters")
    void createsOtterTokenWhenItEnters() {
        castTalent();

        Permanent otter = findPermanent(player1, "Otter");
        assertThat(gqs.getEffectivePower(gd, otter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otter)).isEqualTo(1);
    }

    @Test
    @DisplayName("At level 2, returns a target instant or sorcery from its graveyard to hand")
    void levelTwoReturnsInstantOrSorcery() {
        Card spell = new Shock();
        harness.setGraveyard(player1, List.of(spell));
        Permanent talent = castTalent();

        levelUp(player1, talent, 0, 3);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(spell.getId());

        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("At level 3, creates an Otter token whenever its controller casts an instant or sorcery")
    void levelThreeTriggersOnInstantOrSorceryCast() {
        Card returnedSpell = new Shock();
        harness.setGraveyard(player1, List.of(returnedSpell));
        Permanent talent = castTalent();
        Permanent originalOtter = findPermanent(player1, "Otter");
        levelUp(player1, talent, 0, 3);
        harness.handleMultipleCardsChosen(player1, List.of(returnedSpell.getId()));
        resolveAllTriggers();
        levelUp(player1, talent, 1, 5);

        long tokenCountBeforeCast = countPermanents(player1, "Otter");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Otter")).isEqualTo(tokenCountBeforeCast + 1);
        assertThat(gqs.getEffectivePower(gd, originalOtter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, originalOtter)).isEqualTo(2);
    }

    @Test
    void levelTwoRequiresThreeGenericAndOneBlueMana() {
        Permanent talent = castTalent();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(talent), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void levelThreeRequiresFiveGenericAndOneBlueMana() {
        Permanent talent = castTalent();
        levelUp(player1, talent, 0, 3);
        gd.playerManaPools.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(talent), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainingClassLevelsDoesNotPutCountersOnTheClass() {
        Permanent talent = castTalent();
        levelUp(player1, talent, 0, 3);

        assertThat(talent.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(talent.getClassLevel()).isEqualTo(2);
        levelUp(player1, talent, 1, 5);
        assertThat(talent.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(talent.getClassLevel()).isEqualTo(3);
    }

    @Test
    void otterProwessTriggersForAnEnchantmentAtLevelOne() {
        castTalent();
        Permanent originalOtter = findPermanent(player1, "Otter");
        castTalent();

        assertThat(gqs.getEffectivePower(gd, originalOtter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, originalOtter)).isEqualTo(2);
        assertThat(countPermanents(player1, "Otter")).isEqualTo(2);
    }

    @Test
    void levelTwoTargetsOnlyInstantsAndSorceriesInItsControllersGraveyard() {
        Card sorcery = new PearlOfWisdom();
        Card enchantment = new StormchasersTalent();
        Card opponentSpell = new PearlOfWisdom();
        harness.setGraveyard(player1, List.of(sorcery, enchantment));
        harness.setGraveyard(player2, List.of(opponentSpell));
        Permanent talent = castTalent();
        levelUp(player1, talent, 0, 3);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(sorcery.getId());
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentSpell);
    }

    @Test
    void levelThreeCreatesAnOtterForASorceryWithoutGivingItRetroactiveProwess() {
        Permanent talent = castTalent();
        Permanent originalOtter = findPermanent(player1, "Otter");
        levelUp(player1, talent, 0, 3);
        levelUp(player1, talent, 1, 5);
        harness.setLibrary(player1, List.of(new StormchasersTalent(), new StormchasersTalent()));
        harness.setHand(player1, List.of(new PearlOfWisdom()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Otter")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, originalOtter)).isEqualTo(2);
        Permanent newOtter = findPermanents(player1, "Otter").stream()
                .filter(otter -> otter != originalOtter).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, newOtter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, newOtter)).isEqualTo(1);
    }

    @Test
    void opponentSpellsDoNotTriggerLevelThreeOrOtterProwess() {
        Permanent talent = castTalent();
        Permanent originalOtter = findPermanent(player1, "Otter");
        levelUp(player1, talent, 0, 3);
        levelUp(player1, talent, 1, 5);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Otter")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, originalOtter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, originalOtter)).isEqualTo(1);
    }

    private Permanent castTalent() {
        harness.setHand(player1, List.of(new StormchasersTalent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Stormchaser's Talent");
    }

    private void levelUp(Player player, Permanent talent, int abilityIndex, int genericMana) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, genericMana);
        int talentIndex = gd.playerBattlefields.get(player.getId()).indexOf(talent);
        harness.activateAbility(player, talentIndex, abilityIndex, null, null);
        resolveAllTriggers();
    }

}
