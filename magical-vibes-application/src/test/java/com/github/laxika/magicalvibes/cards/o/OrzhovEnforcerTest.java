package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrotesqueDemise;
import com.github.laxika.magicalvibes.cards.k.KayasWrath;
import com.github.laxika.magicalvibes.cards.s.SenateCourier;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrzhovEnforcer.class, KayasWrath.class, GrotesqueDemise.class, SenateCourier.class})
class OrzhovEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Afterlife creates a 1/1 white and black Spirit token with flying")
    void afterlifeCreatesSpiritToken() {
        harness.addToBattlefield(player1, new OrzhovEnforcer());

        harness.castFromHand(player1, new KayasWrath(), "{W}{W}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Orzhov Enforcer");

        Permanent token = findPermanents(player1, "Spirit").getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Deathtouch kills a blocker even when only one damage is dealt")
    void deathtouchKillsHighToughnessBlocker() {
        Permanent enforcer = addCreatureReady(player1, new OrzhovEnforcer());
        harness.addToBattlefield(player2, new SenateCourier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Senate Courier");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(enforcer);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Each Enforcer creates one Spirit for its own controller after a board wipe")
    void simultaneousDeathsCreateTokensForEachController() {
        harness.addToBattlefield(player1, new OrzhovEnforcer());
        harness.addToBattlefield(player2, new OrzhovEnforcer());

        harness.castFromHand(player1, new KayasWrath(), "{W}{W}{B}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Orzhov Enforcer");
        harness.assertInGraveyard(player2, "Orzhov Enforcer");
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player2, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Exiling Orzhov Enforcer does not trigger afterlife")
    void exileDoesNotCreateSpirit() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OrzhovEnforcer());
        harness.setHand(player1, List.of(new GrotesqueDemise()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, enforcer.getId());
        resolveAllTriggers();

        assertThat(gd.exiledCards)
                .anyMatch(exiled -> exiled.card().getId().equals(enforcer.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enforcer);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }
}
