package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImmolationShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent activating an artifact, creature, or land's non-mana ability deals 1 damage")
    void opponentNonManaAbilityDealsDamage() {
        harness.addToBattlefield(player1, new ImmolationShaman());
        addPermanentWithNonManaAbility(player2, CardType.CREATURE);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A mana ability does not trigger Immolation Shaman")
    void manaAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new ImmolationShaman());
        addPermanentWithAbility(player2, CardType.CREATURE, new AwardManaEffect(ManaColor.GREEN));
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Activated ability gives Immolation Shaman +3/+3 and menace until end of turn")
    void activatedAbilityBoostsAndGrantsMenace() {
        Permanent shaman = addReadyShaman();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, shaman, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Immolation Shaman's activated ability wears off at end of turn")
    void activatedAbilityWearsOffAtEndOfTurn() {
        Permanent shaman = addReadyShaman();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, shaman, Keyword.MENACE)).isFalse();
    }

    private Permanent addReadyShaman() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new ImmolationShaman());
        shaman.setSummoningSick(false);
        return shaman;
    }

    private void addPermanentWithNonManaAbility(Player player, CardType type) {
        addPermanentWithAbility(player, type, new BoostSelfEffect(1, 0));
    }

    private void addPermanentWithAbility(Player player, CardType type, CardEffect effect) {
        Card card = new Card();
        card.setName("Ability Source");
        card.setType(type);
        card.addActivatedAbility(new ActivatedAbility(true, null, List.of(effect), "{T}: ability."));
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }
}
