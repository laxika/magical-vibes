package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YunaGrandSummoner;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChocoboCamp.class, BirdsOfParadise.class, Forest.class, GrizzlyBears.class, YunaGrandSummoner.class})
class ChocoboCampTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped unless you control a legendary creature")
    void entersTappedUnlessLegendaryCreatureIsControlled() {
        harness.setHand(player1, List.of(new ChocoboCamp()));
        harness.playLand(player1, 0);
        assertThat(findPermanent(player1, "Chocobo Camp").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a legendary creature")
    void entersUntappedWithLegendaryCreature() {
        harness.addToBattlefield(player1, new YunaGrandSummoner());
        harness.setHand(player1, List.of(new ChocoboCamp()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Chocobo Camp").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Adds a counter to the next Bird creature spell")
    void nextBirdEntersWithAdditionalCounter() {
        harness.addToBattlefield(player1, new ChocoboCamp());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new BirdsOfParadise()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Birds of Paradise")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not add the Bird counter to a non-Bird creature")
    void ignoresNonBirdCreatureSpells() {
        harness.addToBattlefield(player1, new ChocoboCamp());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Creates a Bird token that gets a temporary landfall boost")
    void createsBirdTokenWithLandfallBoost() {
        harness.addToBattlefield(player1, new ChocoboCamp());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent bird = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BIRD))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(2);
    }
}
