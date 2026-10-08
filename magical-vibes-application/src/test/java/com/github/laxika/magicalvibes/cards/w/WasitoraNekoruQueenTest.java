package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TajuruPreserver;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WasitoraNekoruQueen.class, GrizzlyBears.class, TajuruPreserver.class})
class WasitoraNekoruQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a flying Cat Dragon when the damaged player controls no creature")
    void createsCatDragonWhenDamagedPlayerHasNoCreature() {
        addAttackingWasitora();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat Dragon")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Cat Dragon");
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Makes the damaged player sacrifice their only creature instead of creating a token")
    void sacrificesOnlyCreature() {
        addAttackingWasitora();
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Cat Dragon")).isZero();
    }

    @Test
    @DisplayName("Lets the damaged player choose which creature to sacrifice")
    void damagedPlayerChoosesCreature() {
        addAttackingWasitora();
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        Permanent chosen = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                other.getId(), chosen.getId());

        harness.handlePermanentChosen(player2, chosen.getId());

        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player1, "Cat Dragon")).isZero();
    }

    @Test
    @DisplayName("The created token has the full Cat Dragon characteristics")
    void createsThreeColorThreeThreeFlyingToken() {
        addAttackingWasitora();

        resolveCombat();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Cat Dragon");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(
                CardColor.BLACK, CardColor.RED, CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(
                CardSubtype.CAT, CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        assertThat(countPermanents(player2, "Cat Dragon")).isZero();
    }

    @Test
    @DisplayName("Creates a token when the damaged player cannot sacrifice because of Tajuru Preserver")
    void createsTokenWhenSacrificeIsProhibited() {
        addAttackingWasitora();
        addCreatureReady(player2, new TajuruPreserver());

        resolveCombat();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Tajuru Preserver");
        assertThat(countPermanents(player1, "Cat Dragon")).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A creature entering before the combat damage trigger resolves must be sacrificed")
    void checksCreaturesAtResolution() {
        addAttackingWasitora();

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Cat Dragon")).isZero();
    }

    private Permanent addAttackingWasitora() {
        Permanent wasitora = addCreatureReady(player1, new WasitoraNekoruQueen());
        wasitora.setAttacking(true);
        wasitora.setAttackTarget(player2.getId());
        return wasitora;
    }
}
