package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedArtifactThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "196")
public class DetectivesSatchel extends Card {

    public DetectivesSatchel() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofClueToken(2));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CreateTokenEffect("Thopter", 1, 1, null,
                        List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT))),
                "{T}: Create a 1/1 colorless Thopter artifact creature token with flying. Activate only if "
                        + "you've sacrificed an artifact this turn."
        ).withActivationCondition(new ControllerSacrificedArtifactThisTurn(),
                "Activate only if you've sacrificed an artifact this turn."));
    }
}
