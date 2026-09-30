package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImposterMech.class, AirElemental.class})
class ImposterMechTest extends BaseCardTest {

    @Test
    void copiesOnlyAnOpponentsCreatureAsAnArtifactVehicle() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        ImposterMech mech = new ImposterMech();

        Permanent copiedMech = castAndCopy(mech, opponentCreature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(copiedMech.getCard().getPower()).isEqualTo(opponentCreature.getCard().getPower());
        assertThat(copiedMech.getCard().getToughness()).isEqualTo(opponentCreature.getCard().getToughness());
        assertThat(gqs.isArtifact(gd, copiedMech)).isTrue();
        assertThat(gqs.isCreature(gd, copiedMech)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, copiedMech, CardSubtype.VEHICLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(copiedMech);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
    }

    @Test
    void copiedMechRetainsCrewThree() {
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        crew.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent copiedMech = castAndCopy(new ImposterMech(), target);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(copiedMech), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, copiedMech)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent castAndCopy(ImposterMech mech, Permanent target) {
        harness.setHand(player1, List.of(mech));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(mech.getId()))
                .findFirst()
                .orElseThrow();
    }
}
