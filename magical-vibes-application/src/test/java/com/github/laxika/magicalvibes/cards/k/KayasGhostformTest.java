package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.s.ShallowGrave;
import com.github.laxika.magicalvibes.cards.v.VraskasContempt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KayasGhostform.class, GrizzlyBears.class, GideonBlackblade.class, DoomBlade.class,
        Disperse.class, VraskasContempt.class, PlanarCleansing.class, ShallowGrave.class})
class KayasGhostformTest extends BaseCardTest {

    @Test
    void returnsEnchantedCreatureAfterItDies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castGhostform(creature);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getCard().getId()));
    }

    @Test
    void returnsEnchantedPermanentAfterItIsExiled() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castGhostform(creature);

        harness.setHand(player1, List.of(new VraskasContempt()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getCard().getId()));
    }

    @Test
    void canEnchantAndReturnAPlaneswalker() {
        GideonBlackblade card = new GideonBlackblade();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, card);
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        castGhostform(planeswalker);

        harness.setHand(player1, List.of(new VraskasContempt()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(returned -> returned.getCard().getId().equals(card.getId())
                        && returned.getCounterCount(CounterType.LOYALTY) == 4);
    }

    @Test
    void doesNotReturnAnEnchantedPermanentBouncedToHand() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castGhostform(creature);

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getCard().getId()));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void cannotEnchantAnOpponentPermanent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KayasGhostform()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReturnCreatureAfterAuraWasRemovedEarlier() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castGhostform(creature);
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Kaya's Ghostform"));

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsCreatureWhenAuraAndCreatureAreDestroyedTogether() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castGhostform(creature);
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent aura = battlefield.stream()
                .filter(permanent -> permanent.getCard() instanceof KayasGhostform)
                .findFirst().orElseThrow();
        battlefield.remove(aura);
        battlefield.add(0, aura);

        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Kaya's Ghostform");
        harness.assertNotOnBattlefield(player1, "Kaya's Ghostform");
    }

    @Test
    void doesNotReturnCreatureThatLeftAndReenteredGraveyardBeforeTriggerResolves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castGhostform(creature);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new ShallowGrave()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castGhostform(Permanent target) {
        harness.setHand(player1, List.of(new KayasGhostform()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
