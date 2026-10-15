package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "113")
@CardRegistration(set = "FIN", collectorNumber = "450")
public class RenoAndRude extends Card {

    public RenoAndRude() {
        PermanentPredicate anotherCreatureOrArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsArtifactPredicate()
                )),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, SequenceEffect.of(
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.TARGET_OPPONENT, false),
                new MayEffect(
                        new SacrificePermanentThenEffect(
                                anotherCreatureOrArtifact,
                                new AllowCastAllCardsExiledWithSourceUntilEndOfTurnEffect(null),
                                "another creature or artifact"),
                        "Sacrifice another creature or artifact?")));
    }
}
