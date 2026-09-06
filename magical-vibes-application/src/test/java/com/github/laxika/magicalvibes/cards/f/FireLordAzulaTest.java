package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FireLordAzula.class)
class FireLordAzulaTest extends BaseCardTest {

    @Test
    @DisplayName("Firebending adds red mana until end of combat")
    void firebendingAddsManaUntilEndOfCombat() {
        Permanent azula = addReadyAzula();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(azula);
    }

    @Test
    @DisplayName("Copies an instant cast while Azula is attacking")
    void copiesInstantWhileAttacking() {
        addReadyAzula();
        Card spell = lifeGainInstant();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Keeps the copy trigger after Azula stops attacking")
    void keepsCopyTriggerAfterAzulaStopsAttacking() {
        Permanent azula = addReadyAzula();
        Card spell = lifeGainInstant();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.castInstant(player1, 0);
        azula.setAttacking(false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not copy a spell cast while Azula is not attacking")
    void doesNotCopySpellWhileNotAttacking() {
        addReadyAzula();
        Card spell = lifeGainInstant();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Copies a permanent spell as a token while Azula is attacking")
    void copiesPermanentSpellAsTokenWhileAttacking() {
        addReadyAzula();
        Card spell = flashCreature();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> copies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Flash Creature"))
                .toList();
        assertThat(copies).hasSize(2);
        assertThat(copies).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    private Permanent addReadyAzula() {
        Permanent azula = harness.addToBattlefieldAndReturn(player1, new FireLordAzula());
        azula.setSummoningSick(false);
        return azula;
    }

    private static Card lifeGainInstant() {
        Card card = new Card();
        card.setName("Life Gain");
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.addEffect(EffectSlot.SPELL, new GainLifeEffect(1));
        return card;
    }

    private static Card flashCreature() {
        Card card = new Card();
        card.setName("Flash Creature");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(1);
        card.setToughness(1);
        card.setKeywords(Set.of(Keyword.FLASH));
        return card;
    }
}
