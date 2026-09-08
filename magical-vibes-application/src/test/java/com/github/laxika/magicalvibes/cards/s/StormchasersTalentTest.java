package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({StormchasersTalent.class, Shock.class})
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

        levelUp(player1, talent, 0, 1);

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
        levelUp(player1, talent, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(returnedSpell.getId()));
        resolveAllTriggers();
        levelUp(player1, talent, 1, 3);

        int tokenCountBeforeCast = countOtters(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countOtters(player1)).isEqualTo(tokenCountBeforeCast + 1);
        assertThat(gqs.getEffectivePower(gd, originalOtter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, originalOtter)).isEqualTo(2);
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

    private int countOtters(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Otter"))
                .count();
    }
}
