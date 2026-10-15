package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LanternSpirit;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.cards.y.YavimayaSapherd;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RequiemAngel.class, GrizzlyBears.class, LanternSpirit.class, Shock.class,
        YavimayaSapherd.class, BlasphemousAct.class, Xenograft.class})
class RequiemAngelTest extends BaseCardTest {

    @Test
    @DisplayName("When another non-Spirit creature you control dies, creates a 1/1 white flying Spirit")
    void createsSpiritWhenOwnNonSpiritCreatureDies() {
        harness.addToBattlefield(player1, new RequiemAngel());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player2, player1, "Grizzly Bears");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent spirit = spiritTokens(player1).getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(spirit.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when another Spirit creature you control dies")
    void doesNotTriggerForOwnSpiritCreature() {
        harness.addToBattlefield(player1, new RequiemAngel());
        harness.addToBattlefield(player1, new LanternSpirit());

        killWithShock(player2, player1, "Lantern Spirit");

        assertThat(gd.stack).isEmpty();
        assertThat(spiritTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's non-Spirit creature dies")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new RequiemAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killWithShock(player1, player2, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(spiritTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Triggers when a non-Spirit token creature you control dies")
    void triggersForOwnNonSpiritTokenCreature() {
        harness.addToBattlefield(player1, new RequiemAngel());
        harness.setHand(player1, List.of(new YavimayaSapherd()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        killWithShock(player2, player1, "Saproling");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(spiritTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Its own death does not create a Spirit")
    void doesNotTriggerForItsOwnDeath() {
        harness.addToBattlefield(player1, new RequiemAngel());

        castBlasphemousAct();

        harness.assertInGraveyard(player1, "Requiem Angel");
        assertThat(gd.stack).isEmpty();
        assertThat(spiritTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Sees each other non-Spirit dying simultaneously, even when it also dies")
    void triggersForEachEligibleSimultaneousDeath() {
        harness.addToBattlefield(player1, new RequiemAngel());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LanternSpirit());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castBlasphemousAct();

        harness.assertInGraveyard(player1, "Requiem Angel");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(spiritTokens(player1)).hasSize(2);
        assertThat(spiritTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("Two Angels each see the other Angel die")
    void angelsTriggerForEachOtherWhenBothDie() {
        harness.addToBattlefield(player1, new RequiemAngel());
        harness.addToBattlefield(player1, new RequiemAngel());

        castBlasphemousAct();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(spiritTokens(player1)).hasSize(2);
    }

    @Test
    @DisplayName("A creature made a Spirit by Xenograft does not trigger the Angel when it dies")
    void excludesCreaturesWithSpiritSubtypeGrantedOnBattlefield() {
        harness.addToBattlefield(player1, new RequiemAngel());
        Permanent xenograft = harness.addToBattlefieldAndReturn(player1, new Xenograft());
        xenograft.setChosenSubtype(CardSubtype.SPIRIT);
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player2, player1, "Grizzly Bears");

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(spiritTokens(player1)).isEmpty();
    }

    private void castBlasphemousAct() {
        harness.setHand(player1, List.of(new BlasphemousAct()));
        harness.addMana(player1, ManaColor.RED, 9);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster,
                               com.github.laxika.magicalvibes.model.Player targetController,
                               String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castAndResolveInstant(caster, 0, targetId);
    }

    private List<Permanent> spiritTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getName().equals("Spirit"))
                .toList();
    }
}
