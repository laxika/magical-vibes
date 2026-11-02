package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AnOpponentIsMonarch;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.condition.NoMonarch;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "499")
@CardRegistration(set = "SLD", collectorNumber = "1559")
@CardRegistration(set = "CMM", collectorNumber = "350")
@CardRegistration(set = "CMM", collectorNumber = "588")
@CardRegistration(set = "OTC", collectorNumber = "239")
public class QueenMarchesa extends Card {

    public QueenMarchesa() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BecomeMonarchEffect());

        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new AllConditions(List.of(
                        new NotCondition(new ControllerIsMonarch()),
                        new NotCondition(new NoMonarch()))),
                new CreateTokenEffect("Assassin", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.ASSASSIN), Set.of(Keyword.DEATHTOUCH, Keyword.HASTE), Set.of())));
    }
}
