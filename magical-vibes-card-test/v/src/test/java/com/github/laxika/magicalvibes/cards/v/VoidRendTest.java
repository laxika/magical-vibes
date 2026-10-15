package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AnOfferYouCantRefuse;
import com.github.laxika.magicalvibes.cards.b.BrokersInitiate;
import com.github.laxika.magicalvibes.cards.d.DisciplinedDuelist;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.q.QuickDrawDagger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoidRend.class, BrokersInitiate.class, Forest.class, AnOfferYouCantRefuse.class,
        QuickDrawDagger.class, DisciplinedDuelist.class})
class VoidRendTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target nonland permanent")
    void destroysTargetNonlandPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BrokersInitiate()).getId();

        resolveVoidRend(targetId);

        harness.assertNotOnBattlefield(player2, "Brokers Initiate");
        harness.assertInGraveyard(player2, "Brokers Initiate");
        harness.assertInGraveyard(player1, "Void Rend");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        harness.setHand(player1, List.of(new VoidRend()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        VoidRend voidRend = new VoidRend();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BrokersInitiate()).getId();

        harness.setHand(player1, List.of(voidRend));
        addMana();
        harness.setHand(player2, List.of(new AnOfferYouCantRefuse()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, voidRend.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Void Rend");
        harness.assertInGraveyard(player2, "An Offer You Can't Refuse");
        harness.assertInGraveyard(player2, "Brokers Initiate");
    }

    @Test
    @DisplayName("Destroys a noncreature artifact")
    void destroysArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new QuickDrawDagger()).getId();

        resolveVoidRend(targetId);

        harness.assertNotOnBattlefield(player2, "Quick-Draw Dagger");
        harness.assertInGraveyard(player2, "Quick-Draw Dagger");
        harness.assertInGraveyard(player1, "Void Rend");
    }

    @Test
    @DisplayName("Can destroy a permanent its controller controls")
    void destroysOwnPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new BrokersInitiate()).getId();

        resolveVoidRend(targetId);

        harness.assertNotOnBattlefield(player1, "Brokers Initiate");
        harness.assertInGraveyard(player1, "Brokers Initiate");
        harness.assertInGraveyard(player1, "Void Rend");
    }

    @Test
    @DisplayName("Does not resolve when its only target has left the battlefield")
    void doesNotResolveWithMissingTarget() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BrokersInitiate()).getId();
        VoidRend first = new VoidRend();
        VoidRend second = new VoidRend();
        harness.setHand(player1, List.of(first, second));
        addMana();
        addMana();

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertNotOnBattlefield(player2, "Brokers Initiate");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        harness.assertInGraveyard(player2, "Brokers Initiate");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A shield counter replaces destruction even though Void Rend cannot be countered")
    void shieldCounterPreventsDestruction() {
        Permanent duelist = harness.enterBattlefieldAndReturn(player2, new DisciplinedDuelist());

        resolveVoidRend(duelist.getId());

        harness.assertOnBattlefield(player2, "Disciplined Duelist");
        harness.assertNotInGraveyard(player2, "Disciplined Duelist");
        assertThat(duelist.getCounterCount(CounterType.SHIELD)).isZero();
        harness.assertInGraveyard(player1, "Void Rend");

        resolveVoidRend(duelist.getId());

        harness.assertNotOnBattlefield(player2, "Disciplined Duelist");
        harness.assertInGraveyard(player2, "Disciplined Duelist");
    }

    private void resolveVoidRend(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new VoidRend()));
        addMana();
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
