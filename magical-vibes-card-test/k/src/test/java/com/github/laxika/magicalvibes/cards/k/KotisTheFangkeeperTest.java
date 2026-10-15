package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KotisTheFangkeeper.class, Divination.class, GrizzlyBears.class, Forest.class})
class KotisTheFangkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles that many cards and only grants free casts up to that mana value")
    void exilesDamageAmountAndLimitsFreeCasts() {
        Permanent kotis = addAttackingKotis();
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new Divination();
        harness.setLibrary(player2, List.of(eligible, tooExpensive));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(kotis.getId()))
                .extracting(Card::getId)
                .containsExactly(eligible.getId(), tooExpensive.getId());
        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds()).contains(eligible.getId());
        assertThat(interaction.validCardIds()).doesNotContain(tooExpensive.getId());
    }

    @Test
    @DisplayName("Any number of eligible exiled spells can be cast without paying their mana costs")
    void castsAnyNumberOfEligibleExiledSpells() {
        addAttackingKotis();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second));

        resolveCombatAndTrigger();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(gd.findExiledCard(first.getId())).isNull();
        assertThat(gd.findExiledCard(second.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Declining all spells leaves them exiled and ends the casting choice")
    void mayDeclineAllSpells() {
        addAttackingKotis();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second));

        resolveCombatAndTrigger();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A short library exiles only available cards without offering expensive spells")
    void shortLibraryWithNoEligibleSpells() {
        addAttackingKotis();
        KotisTheFangkeeper expensive = new KotisTheFangkeeper();
        harness.setLibrary(player2, List.of(expensive));

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(expensive.getId())).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exile count and mana value limit use damage dealt before Kotis's power changes")
    void usesDamageAmountRatherThanPowerAtResolution() {
        Permanent kotis = addAttackingKotis();
        kotis.setPowerModifier(1);
        Divination first = new Divination();
        Divination second = new Divination();
        Divination third = new Divination();
        GrizzlyBears remaining = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second, third, remaining));

        resolveCombat();
        kotis.setPowerModifier(0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        PendingInteraction.ImprovisationCapstoneCastChoice interaction =
                (PendingInteraction.ImprovisationCapstoneCastChoice) gd.interaction.activeInteraction();
        assertThat(interaction.validCardIds())
                .containsExactly(first.getId(), second.getId(), third.getId());
    }

    @Test
    @DisplayName("Kotis's trigger still exiles and casts spells after Kotis leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent kotis = addAttackingKotis();
        GrizzlyBears spell = new GrizzlyBears();
        harness.setLibrary(player2, List.of(spell));

        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(kotis);
        gd.playerGraveyards.get(player1.getId()).add(kotis.getCard());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        resolveAllTriggers();

        assertThat(gd.findExiledCard(spell.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(spell.getId());
    }

    @Test
    @DisplayName("Exiled lands are not offered as spells")
    void cannotPlayExiledLands() {
        addAttackingKotis();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player2, List.of(first, second));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Zero combat damage does not trigger Kotis")
    void doesNotTriggerForZeroDamage() {
        Permanent kotis = addAttackingKotis();
        kotis.setPowerModifier(-2);
        KotisTheFangkeeper top = new KotisTheFangkeeper();
        harness.setLibrary(player2, List.of(top));

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
        assertThat(gd.findExiledCard(top.getId())).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An eligible sorcery can be cast during combat without paying mana")
    void castsSorceryDuringCombat() {
        Permanent kotis = addAttackingKotis();
        kotis.setPowerModifier(1);
        Divination spell = new Divination();
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setLibrary(player2, List.of(spell));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        resolveCombatAndTrigger();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
        assertThat(gd.findExiledCard(spell.getId())).isNull();
    }

    private Permanent addAttackingKotis() {
        Permanent kotis = addCreatureReady(player1, new KotisTheFangkeeper());
        kotis.setAttacking(true);
        return kotis;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
