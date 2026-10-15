package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.r.RendFlesh;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

@CardUsed({Frostwielder.class, GlacialRay.class, IsamaruHoundOfKonda.class, LanternKami.class, RendFlesh.class})
class FrostwielderTest extends BaseCardTest {

    private boolean isExiled(String cardName) {
        return gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals(cardName));
    }

    @Test
    @DisplayName("A creature killed by the ping is exiled instead of dying")
    void pingedCreatureIsExiled() {
        addCreatureReady(player1, new Frostwielder());
        harness.addToBattlefield(player2, new LanternKami());

        UUID targetId = harness.getPermanentId(player2, "Lantern Kami");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Lantern Kami");
        harness.assertNotInGraveyard(player2, "Lantern Kami");
        assertThat(isExiled("Lantern Kami")).isTrue();
    }

    @Test
    @DisplayName("A creature damaged earlier by Frostwielder is exiled when another source finishes it")
    void creatureDamagedEarlierIsExiled() {
        addCreatureReady(player1, new Frostwielder());
        addCreatureReady(player1, new Frostwielder());
        harness.addToBattlefield(player2, new IsamaruHoundOfKonda());

        UUID targetId = harness.getPermanentId(player2, "Isamaru, Hound of Konda");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Isamaru, Hound of Konda");

        harness.activateAbility(player1, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Isamaru, Hound of Konda");
        assertThat(isExiled("Isamaru, Hound of Konda")).isTrue();
    }

    @Test
    @DisplayName("A creature Frostwielder never damaged dies to the graveyard normally")
    void undamagedCreatureGoesToGraveyard() {
        addCreatureReady(player1, new Frostwielder());
        harness.addToBattlefield(player2, new LanternKami());

        UUID targetId = harness.getPermanentId(player2, "Lantern Kami");
        castGlacialRay(targetId);

        harness.assertInGraveyard(player2, "Lantern Kami");
    }

    @Test
    @DisplayName("The ping can target a player")
    void pingCanTargetPlayer() {
        addCreatureReady(player1, new Frostwielder());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The replacement stops applying once Frostwielder has left the battlefield")
    void replacementStopsWhenFrostwielderLeaves() {
        Permanent frostwielder = addCreatureReady(player1, new Frostwielder());
        harness.addToBattlefield(player2, new IsamaruHoundOfKonda());

        UUID targetId = harness.getPermanentId(player2, "Isamaru, Hound of Konda");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, frostwielder));

        castGlacialRay(targetId);

        harness.assertInGraveyard(player2, "Isamaru, Hound of Konda");
    }

    private void castGlacialRay(UUID targetId) {
        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    @Test
    @DisplayName("Damage from an unrelated spell still causes exile after Frostwielder's ping")
    void unrelatedSpellFinishesDamagedCreature() {
        addCreatureReady(player1, new Frostwielder());
        harness.addToBattlefield(player2, new IsamaruHoundOfKonda());
        UUID targetId = harness.getPermanentId(player2, "Isamaru, Hound of Konda");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        castGlacialRay(targetId);

        harness.assertNotOnBattlefield(player2, "Isamaru, Hound of Konda");
        harness.assertNotInGraveyard(player2, "Isamaru, Hound of Konda");
        assertThat(isExiled("Isamaru, Hound of Konda")).isTrue();
    }

    @Test
    @DisplayName("Frostwielder also exiles a friendly creature it kills")
    void friendlyCreatureIsExiled() {
        addCreatureReady(player1, new Frostwielder());
        harness.addToBattlefield(player1, new LanternKami());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Lantern Kami"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lantern Kami");
        harness.assertNotInGraveyard(player1, "Lantern Kami");
        assertThat(isExiled("Lantern Kami")).isTrue();
    }

    @Test
    @DisplayName("Frostwielder's replacement applies to itself after self-inflicted damage")
    void selfDamagedFrostwielderIsExiled() {
        Permanent frostwielder = addCreatureReady(player1, new Frostwielder());

        harness.activateAbility(player1, 0, null, frostwielder.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Frostwielder");
        castGlacialRay(frostwielder.getId());

        harness.assertNotOnBattlefield(player1, "Frostwielder");
        harness.assertNotInGraveyard(player1, "Frostwielder");
        assertThat(isExiled("Frostwielder")).isTrue();
    }

    @Test
    @DisplayName("An activated ping still resolves after its source leaves, without exiling the victim")
    void pingResolvesAfterSourceLeaves() {
        Permanent frostwielder = addCreatureReady(player1, new Frostwielder());
        harness.addToBattlefield(player2, new LanternKami());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Lantern Kami"));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, frostwielder));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Lantern Kami");
        harness.assertInGraveyard(player2, "Lantern Kami");
        assertThat(isExiled("Lantern Kami")).isFalse();
    }

    @Test
    @DisplayName("A targeted destroy spell exiles Frostwielder after it has damaged itself")
    void selfDamagedFrostwielderIsExiledWhenDestroyed() {
        Permanent frostwielder = addCreatureReady(player1, new Frostwielder());
        harness.activateAbility(player1, 0, null, frostwielder.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new RendFlesh()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, frostwielder.getId());

        harness.assertNotOnBattlefield(player1, "Frostwielder");
        harness.assertNotInGraveyard(player1, "Frostwielder");
        assertThat(isExiled("Frostwielder")).isTrue();
    }
}
