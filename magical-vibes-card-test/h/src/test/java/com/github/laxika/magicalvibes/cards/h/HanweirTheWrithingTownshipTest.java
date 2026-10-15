package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HanweirTheWrithingTownship.class, HanweirBattlements.class, HanweirGarrison.class, InvasionOfInnistrad.class})
class HanweirTheWrithingTownshipTest extends BaseCardTest {

    @Test
    void canAttackImmediatelyAfterMeldingAndCreatesTappedAttackingHorrors() {
        meldTownship();
        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertHorrors();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackTriggerStillCreatesTokensAfterTownshipLeaves() {
        Permanent township = meldTownship();
        declareAttackers(List.of(0));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, township));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hanweir, the Writhing Township");
        assertHorrors();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactlyInAnyOrder("Hanweir Battlements", "Hanweir Garrison");
    }

    @Test
    void createsNoTokensWhenNotDeclaredAsAttacker() {
        meldTownship();
        declareAttackers(List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void exilingMeldedTownshipExilesBothFrontFaces() {
        Permanent township = meldTownship();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, township));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactlyInAnyOrder("Hanweir Battlements", "Hanweir Garrison");
    }

    @Test
    void tokensCanAttackABattleProtectedByTheOpponent() {
        meldTownship();
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        declareAttackers(List.of(0));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, battle.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
        });
        assertThat(tokens).extracting(Permanent::getAttackTarget)
                .containsExactlyInAnyOrder(battle.getId(), player2.getId());
    }
    private Permanent meldTownship() {
        harness.addToBattlefield(player1, new HanweirBattlements());
        harness.addToBattlefield(player1, new HanweirGarrison());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        return findPermanent(player1, "Hanweir, the Writhing Township");
    }

    private void assertHorrors() {
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(2);
        tokens.forEach(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
            assertThat(token.getCard().getPower()).isEqualTo(3);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColors()).isEmpty();
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.HORROR);
        });
    }
}
