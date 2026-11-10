package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "Whenever this Equipment becomes unattached from a permanent" is a triggered ability the card writes
 * ({@code Triggers.becomesUnattachedFromPermanent()}): one object of all its effects (CR 603.3) that records the
 * permanent it was attached to as "that permanent", two abilities two objects, and an Equipment without the ability
 * has none.
 */
@CardUsed({GrizzlyBears.class})
class BecomesUnattachedTriggerTest extends BaseCardTest {

    private static Card equipment(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        card.setManaCost("{1}");
        return card;
    }

    private static PermanentRemovalService removal() {
        return GameTestEngineContext.get().getBean(PermanentRemovalService.class);
    }

    private List<StackEntry> entriesOf(String name) {
        return gd.stack.stream().filter(entry -> entry.getCard().getName().equals(name)).toList();
    }

    private Permanent attached(Card card, Permanent host) {
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setAttachedTo(host.getId());
        return permanent;
    }

    @Test
    @DisplayName("\"Becomes unattached from a permanent, sacrifice that permanent and you gain 1 life\" is one stack object naming that permanent")
    void unattachAbilityIsOneStackObjectNamingThePermanent() {
        Card card = equipment("Grafted Hook");
        card.addTrigger(Triggers.becomesUnattachedFromPermanent(), new SacrificeTriggeringPermanentEffect(),
                new GainLifeEffect(1));
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent hook = attached(card, bears);
        int lifeBefore = gd.getLife(player1.getId());

        harness.inMutationScope(() -> removal().tryDestroyPermanent(gd, hook));

        assertThat(entriesOf("Grafted Hook")).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).hasSize(2);
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(bears.getId());
        });
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Two \"becomes unattached\" abilities are two stack objects, and an Equipment without one has none")
    void twoUnattachAbilitiesAreTwoStackObjects() {
        Card twin = equipment("Twin Hook");
        twin.addTrigger(Triggers.becomesUnattachedFromPermanent(), new GainLifeEffect(1));
        twin.addTrigger(Triggers.becomesUnattachedFromPermanent(), new GainLifeEffect(2));
        Card plain = equipment("Plain Hook");
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent twinHook = attached(twin, bears);
        Permanent plainHook = attached(plain, bears);

        harness.inMutationScope(() -> removal().tryDestroyPermanent(gd, twinHook));
        harness.inMutationScope(() -> removal().tryDestroyPermanent(gd, plainHook));

        assertThat(entriesOf("Twin Hook")).hasSize(2);
        assertThat(entriesOf("Plain Hook")).isEmpty();
    }
}
