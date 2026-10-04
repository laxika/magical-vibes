package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.c.CivicWayfinder;
import com.github.laxika.magicalvibes.cards.g.GladeOfThePumpSpells;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Friarball.class, Forest.class, GrizzlyBears.class, Shock.class, Blaze.class,
        GladeOfThePumpSpells.class, CivicWayfinder.class})
class FriarballTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one 2/2 white Monk token when no other cards were played")
    void createsOneMonkToken() {
        castFriarball();

        assertThat(monkTokens()).hasSize(1);
        assertThat(monkTokens().getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.MONK);
        assertThat(monkTokens().getFirst().getCard().getColor()).isEqualTo(com.github.laxika.magicalvibes.model.CardColor.WHITE);
    }

    @Test
    @DisplayName("Coststorm copies for each distinct prior spell and land mana value")
    void coststormCopiesForDistinctManaValues() {
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player1.getId(), new Shock());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        castFriarball();

        assertThat(monkTokens()).hasSize(4);
    }

    private void castFriarball() {
        harness.castFromHand(player1, new Friarball(), "{3}{W}");
        resolveAllTriggers();
    }

    @Test
    void countsSpellsCastInResponseToCoststorm() {
        harness.castFromHand(player1, new Friarball(), "{3}{W}");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, player2.getId());

        resolveAllTriggers();

        assertThat(monkTokens()).hasSize(2);
    }

    @Test
    void doesNotCountOpponentsSpells() {
        gd.recordSpellCast(player2.getId(), new Shock());

        castFriarball();

        assertThat(monkTokens()).hasSize(1);
        assertThat(monkTokens().getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(monkTokens().getFirst().getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void landAndSpellWithTheSameManaValueCountOnce() {
        harness.setHand(player1, List.of(new GladeOfThePumpSpells()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.playLand(player1, 0);
        resolveAllTriggers();
        gd.recordSpellCast(player1.getId(), new CivicWayfinder());

        castFriarball();

        assertThat(monkTokens()).hasSize(2);
    }

    @Test
    void remembersChosenXInPriorSpellsManaValues() {
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        castFriarball();

        assertThat(monkTokens()).hasSize(3);
    }

    private List<Permanent> monkTokens() {
        return findPermanents(player1, "Monk").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
