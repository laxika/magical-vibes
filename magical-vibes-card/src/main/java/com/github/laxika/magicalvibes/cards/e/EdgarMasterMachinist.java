package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "80")
@CardRegistration(set = "FIC", collectorNumber = "169")
public class EdgarMasterMachinist extends Card {

    public EdgarMasterMachinist() {
        CardAllOfPredicate artifactSpell = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));

        // Once during each of your turns, you may cast an artifact spell from your graveyard.
        // Artifact spells cast this way enter the battlefield tapped.
        addEffect(EffectSlot.STATIC, new PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
                artifactSpell, null, true));

        // Tools — Whenever Edgar attacks, it gets +X/+0 until end of turn, where X is the
        // greatest mana value among artifacts you control.
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(
                new GreatestManaValueAmongControlled(new PermanentIsArtifactPredicate()),
                new Fixed(0)));
    }
}
