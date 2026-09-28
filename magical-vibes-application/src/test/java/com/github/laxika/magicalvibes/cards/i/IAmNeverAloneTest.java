package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThePrismaticPiper;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IAmNeverAlone.class, GrizzlyBears.class, ThePrismaticPiper.class})
class IAmNeverAloneTest extends BaseCardTest {

    @Test
    void createsNonlegendaryTokenCopyOfCommanderInCommandZone() {
        ThePrismaticPiper commander = prepareCommanderInCommandZone();

        resolveScheme();

        Permanent token = findPermanents(player1, commander.getName()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(gd.isCommander(token.getCard().getId())).isFalse();
    }

    @Test
    void copiesCommanderPermanentAndPreservesItsPrintedCharacteristics() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = addCreatureReady(player1, commander);

        resolveScheme();

        Permanent token = findPermanents(player1, commanderPermanent.getCard().getName()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(commanderPermanent.getEffectivePower());
        assertThat(token.getEffectiveToughness()).isEqualTo(commanderPermanent.getEffectiveToughness());
    }

    @Test
    void doesNothingWithoutACommander() {
        resolveScheme();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private ThePrismaticPiper prepareCommanderInCommandZone() {
        ThePrismaticPiper commander = new ThePrismaticPiper();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        return commander;
    }

    private void resolveScheme() {
        IAmNeverAlone scheme = new IAmNeverAlone();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }
}
