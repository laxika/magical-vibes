package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HiddenHerbalists.class, GrizzlyBears.class})
class HiddenHerbalistsTest extends BaseCardTest {

    @Test
    @DisplayName("Adds {G}{G} if a permanent you controlled left the battlefield this turn")
    void addsManaAfterYourPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.setHand(player1, List.of(new HiddenHerbalists()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not add mana when no permanent left the battlefield")
    void doesNotAddManaWithoutRevolt() {
        harness.setHand(player1, List.of(new HiddenHerbalists()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("An opponent's permanent leaving the battlefield does not satisfy revolt")
    void opponentPermanentLeavingDoesNotSatisfyRevolt() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.setHand(player1, List.of(new HiddenHerbalists()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("A permanent leaving after Herbalists enters cannot enable its ability retroactively")
    void leavingAfterEntryDoesNotEnableRevolt() {
        Permanent herbalists = harness.enterBattlefieldAndReturn(player1, new HiddenHerbalists());

        assertThat(gd.stack).isEmpty();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, herbalists));
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Revolt ability uses the stack and still adds mana after Herbalists leaves")
    void addsManaAfterSourceLeavesBeforeResolution() {
        Permanent earlierPermanent = harness.addToBattlefieldAndReturn(player1, new HiddenHerbalists());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, earlierPermanent));

        Permanent herbalists = harness.enterBattlefieldAndReturn(player1, new HiddenHerbalists());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, herbalists));
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple permanents leaving still award exactly two green mana")
    void multipleDeparturesDoNotMultiplyMana() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HiddenHerbalists());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HiddenHerbalists());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
            harness.getPermanentRemovalService().removePermanentToExile(gd, second);
        });

        harness.enterBattlefieldAndReturn(player1, new HiddenHerbalists());
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }
}
