package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CloakTopCardOfDamagedPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TurnSourceFaceUpOrExileAndMayCastEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

@CardRegistration(set = "MKM", collectorNumber = "200")
@CardRegistration(set = "MKM", collectorNumber = "359")
@CardRegistration(set = "MKM", collectorNumber = "386")
public class EtrataDeadlyFugitive extends Card {

    public EtrataDeadlyFugitive() {
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{2}{U}{B}",
                        List.of(new TurnSourceFaceUpOrExileAndMayCastEffect()),
                        "{2}{U}{B}: Turn this creature face up. If you can't, exile it, then you may cast the exiled card without paying its mana cost."
                ),
                GrantScope.OWN_CREATURES,
                new PermanentIsFaceDownPredicate()
        ));

        addTrigger(Triggers.creatureDealsCombatDamageToOpponent().withFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.ASSASSIN)))),
                new CloakTopCardOfDamagedPlayerLibraryEffect());
    }
}
