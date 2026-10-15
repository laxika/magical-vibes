package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoundByMoonsilver;
import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SecondHarvest.class, ThrabenInspector.class, BoundByMoonsilver.class})
class SecondHarvestTest extends BaseCardTest {

    private List<Permanent> tokensNamed(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals(name))
                .toList();
    }

    private Permanent addToken(Player player, String name, CardType type, List<CardSubtype> subtypes) {
        Card card = new Card();
        card.setToken(true);
        card.setName(name);
        card.setType(type);
        if (type == CardType.CREATURE) {
            card.setPower(1);
            card.setToughness(1);
        }
        card.setSubtypes(subtypes);
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Copies each creature token you control; nontokens are ignored")
    void copiesEachCreatureToken() {
        addToken(player1, "Elf Warrior", CardType.CREATURE, List.of(CardSubtype.ELF, CardSubtype.WARRIOR));
        addToken(player1, "Elf Warrior", CardType.CREATURE, List.of(CardSubtype.ELF, CardSubtype.WARRIOR));
        addCreatureReady(player1, new ThrabenInspector());
        harness.setHand(player1, List.of(new SecondHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(tokensNamed(player1, "Elf Warrior")).hasSize(4);
        assertThat(countPermanents(player1, "Thraben Inspector")).isEqualTo(1);
    }

    @Test
    @DisplayName("Also copies noncreature tokens you control")
    void copiesNoncreatureTokens() {
        addToken(player1, "Treasure", CardType.ARTIFACT, List.of(CardSubtype.TREASURE));
        addToken(player1, "Saproling", CardType.CREATURE, List.of(CardSubtype.SAPROLING));
        harness.setHand(player1, List.of(new SecondHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(tokensNamed(player1, "Treasure")).hasSize(2);
        assertThat(tokensNamed(player1, "Saproling")).hasSize(2);
    }

    @Test
    @DisplayName("With no tokens, creates nothing")
    void withNoTokensCreatesNothing() {
        addCreatureReady(player1, new ThrabenInspector());
        harness.setHand(player1, List.of(new SecondHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        int before = gd.playerBattlefields.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(before);
    }

    @Test
    void doesNotCopyOpponentsTokens() {
        addToken(player1, "Spirit", CardType.CREATURE, List.of(CardSubtype.SPIRIT));
        addToken(player2, "Spirit", CardType.CREATURE, List.of(CardSubtype.SPIRIT));
        addToken(player2, "Clue", CardType.ARTIFACT, List.of(CardSubtype.CLUE));
        harness.setHand(player1, List.of(new SecondHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(tokensNamed(player1, "Spirit")).hasSize(2);
        assertThat(tokensNamed(player1, "Clue")).isEmpty();
        assertThat(tokensNamed(player2, "Spirit")).hasSize(1);
        assertThat(tokensNamed(player2, "Clue")).hasSize(1);
    }

    @Test
    void doesNotCopyCountersDamageTappedStateOrTemporaryBoosts() {
        Permanent original = addToken(player1, "Spirit", CardType.CREATURE, List.of(CardSubtype.SPIRIT));
        original.tap();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        original.setPowerModifier(3);
        original.setToughnessModifier(3);
        original.setMarkedDamage(1);
        harness.setHand(player1, List.of(new SecondHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> copies = tokensNamed(player1, "Spirit").stream()
                .filter(p -> !p.getId().equals(original.getId()))
                .toList();
        assertThat(copies).hasSize(1);
        Permanent copy = copies.getFirst();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.isSummoningSick()).isTrue();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(copy.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(1);
        assertThat(original.isTapped()).isTrue();
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void copiedClueRetainsItsActivatedAbility() {
        harness.setHand(player1, List.of(new ThrabenInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent originalClue = findPermanent(player1, "Clue");
        harness.setHand(player1, List.of(new SecondHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(tokensNamed(player1, "Clue")).hasSize(2);
        Permanent copy = tokensNamed(player1, "Clue").stream()
                .filter(p -> !p.getId().equals(originalClue.getId()))
                .findFirst().orElseThrow();
        harness.setLibrary(player1, List.of(new ThrabenInspector()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(copy);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();

        assertThat(tokensNamed(player1, "Clue")).containsExactly(originalClue);
        harness.assertInHand(player1, "Thraben Inspector");
    }

    @Test
    void copiedAuraEntersAttachedToChosenCreature() {
        Permanent first = addCreatureReady(player1, new ThrabenInspector());
        Permanent second = addCreatureReady(player2, new ThrabenInspector());
        Card auraCard = new BoundByMoonsilver().createRuntimeCopyWithNewId();
        auraCard.setToken(true);
        Permanent original = harness.addToBattlefieldAndReturn(player1, auraCard);
        original.setAttachedTo(first.getId());
        harness.setHand(player1, List.of(new SecondHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();

        assertThat(tokensNamed(player1, "Bound by Moonsilver")).hasSize(2);
        Permanent copy = tokensNamed(player1, "Bound by Moonsilver").stream()
                .filter(p -> !p.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.getAttachedTo()).isEqualTo(second.getId());
        assertThat(original.getAttachedTo()).isEqualTo(first.getId());
    }
}
