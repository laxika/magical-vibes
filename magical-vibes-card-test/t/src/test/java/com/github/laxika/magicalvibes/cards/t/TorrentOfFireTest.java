package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FormOfTheDragon;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TorrentOfFire.class, GoblinBrigand.class, TwistedAbomination.class,
        TempleOfTheFalseGod.class, FormOfTheDragon.class})
class TorrentOfFireTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToGreatestManaValueAmongYourPermanents() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GoblinBrigand());
        harness.addToBattlefield(player1, new TwistedAbomination());
        harness.setHand(player1, List.of(new TorrentOfFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    void countsOnlyPermanentsControlledByTheSpellController() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GoblinBrigand());
        harness.addToBattlefield(player2, new TwistedAbomination());
        harness.setHand(player1, List.of(new TorrentOfFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void dealsNoDamageWhenYouControlNoPermanents() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new TorrentOfFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void evaluatesGreatestManaValueAtResolutionAndCanTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TwistedAbomination());
        harness.addToBattlefield(player1, new TwistedAbomination());
        harness.setHand(player1, List.of(new TorrentOfFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new GoblinBrigand());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void includesNoncreaturePermanentsWhenDeterminingDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FormOfTheDragon());
        harness.addToBattlefield(player1, new TwistedAbomination());
        harness.setHand(player1, List.of(new TorrentOfFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
    }

    @Test
    void dealsNoDamageWhenOnlyLandsAreControlled() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        harness.setHand(player1, List.of(new TorrentOfFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void includesPermanentsThatEnterBeforeResolution() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GoblinBrigand());
        harness.setHand(player1, List.of(new TorrentOfFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new TwistedAbomination());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    void canDealLethalDamageToYourOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TwistedAbomination());
        harness.setHand(player1, List.of(new TorrentOfFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Twisted Abomination");
        harness.assertInGraveyard(player1, "Twisted Abomination");
    }

    @Test
    void cannotTargetALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TempleOfTheFalseGod());
        harness.setHand(player1, List.of(new TorrentOfFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
