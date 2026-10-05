package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.cards.s.SpectralProcession;
import com.github.laxika.magicalvibes.cards.t.TamiyoCompleatedSage;
import com.github.laxika.magicalvibes.cards.t.TurnToMist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProvidenceOfNight.class, FlameJavelin.class, SafeholdElite.class,
        SpectralProcession.class, TamiyoCompleatedSage.class, TurnToMist.class})
class ProvidenceOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a spell with hybrid mana in its mana cost")
    void copiesHybridManaSpell() {
        harness.addToBattlefield(player1, new ProvidenceOfNight());
        harness.setHand(player1, List.of(lifeGainInstant("{W/U}")));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Does not copy a spell without hybrid mana")
    void doesNotCopyNonHybridSpell() {
        harness.addToBattlefield(player1, new ProvidenceOfNight());
        harness.setHand(player1, List.of(lifeGainInstant("{1}")));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Has protection from monocolored sources")
    void hasProtectionFromMonocoloredSources() {
        Permanent providence = harness.addToBattlefieldAndReturn(player1, new ProvidenceOfNight());
        Permanent monocoloredSource = coloredSource(List.of(CardColor.RED));
        Permanent multicoloredSource = coloredSource(List.of(CardColor.RED, CardColor.BLUE));

        assertThat(gqs.hasProtectionFromSource(gd, providence, monocoloredSource)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, providence, multicoloredSource)).isFalse();
    }

    @Test
    void copiesHybridCreatureSpellAsTokenWithoutCastingTheCopy() {
        harness.addToBattlefield(player1, new ProvidenceOfNight());
        harness.setHand(player1, List.of(new SafeholdElite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> elites = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Safehold Elite"))
                .toList();
        assertThat(elites).hasSize(2);
        assertThat(elites.stream().filter(p -> p.getCard().isToken()).count()).isEqualTo(1);
        assertThat(elites.stream().filter(Permanent::isCast).count()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCopyOpponentsHybridCreatureSpell() {
        harness.addToBattlefield(player1, new ProvidenceOfNight());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SafeholdElite()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Safehold Elite"))
                .hasSize(1)
                .allMatch(p -> !p.getCard().isToken());
        harness.assertNotOnBattlefield(player1, "Safehold Elite");
    }

    @Test
    void copiesSpellWithHybridPhyrexianMana() {
        harness.addToBattlefield(player1, new ProvidenceOfNight());
        harness.setHand(player1, List.of(new TamiyoCompleatedSage()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.stream().filter(e -> e.isCopy()).count()).isEqualTo(1);
    }

    @Test
    void mayKeepOriginalTargetForHybridSpellCopy() {
        harness.addToBattlefield(player1, new ProvidenceOfNight());
        Permanent elite = harness.addToBattlefieldAndReturn(player2, new SafeholdElite());
        harness.setHand(player1, List.of(new TurnToMist()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, elite.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Safehold Elite");
        harness.assertOnBattlefield(player1, "Providence of Night");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Safehold Elite"))
                .hasSize(1);
    }

    @Test
    void mayChooseNewTargetForHybridSpellCopy() {
        Permanent providence = harness.addToBattlefieldAndReturn(player1, new ProvidenceOfNight());
        Permanent elite = harness.addToBattlefieldAndReturn(player2, new SafeholdElite());
        harness.setHand(player1, List.of(new TurnToMist()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, elite.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, providence.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Providence of Night");
        harness.assertNotOnBattlefield(player2, "Safehold Elite");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Providence of Night"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Safehold Elite"));
    }

    @Test
    void protectionDoesNotApplyToColorlessSources() {
        Permanent providence = harness.addToBattlefieldAndReturn(player1, new ProvidenceOfNight());

        assertThat(gqs.hasProtectionFromSource(gd, providence, coloredSource(List.of()))).isFalse();
    }

    @Test
    void copiesMonocoloredHybridSpellPaidWithGenericMana() {
        harness.addToBattlefield(player1, new ProvidenceOfNight());
        harness.setHand(player1, List.of(new SpectralProcession()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .hasSize(6);
    }

    @Test
    void cannotBeTargetedByMonocoloredHybridSpell() {
        Permanent providence = harness.addToBattlefieldAndReturn(player1, new ProvidenceOfNight());
        harness.setHand(player1, List.of(new FlameJavelin()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, providence.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Providence of Night");
        harness.assertInHand(player1, "Flame Javelin");
    }

    private static Permanent coloredSource(List<CardColor> colors) {
        Card card = new Card();
        card.setColors(colors);
        return new Permanent(card);
    }

    private static Card lifeGainInstant(String manaCost) {
        Card card = new Card();
        card.setName("Test Life Gain");
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.addEffect(EffectSlot.SPELL, new GainLifeEffect(1));
        return card;
    }
}
